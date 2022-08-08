package ru.medvedev.importer.service.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.medvedev.importer.component.TelegramProperty;
import ru.medvedev.importer.dto.events.CheckBotColumnResponseEvent;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.FileSource;
import ru.medvedev.importer.enums.XlsxRequireField;
import ru.medvedev.importer.service.FileInfoService;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static ru.medvedev.importer.utils.StringUtils.transformTgMessage;


@RequiredArgsConstructor
@Service
@Log4j2
public class TelegramPollingService extends TelegramLongPollingBot {

    @Value("${directory.upload-dir}")
    private String uploadDir;
    @Value("${telegram.scanningChatId}")
    private Long scanningChatId;

    private final TelegramProperty properties;
    private final FileInfoService fileInfoService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public String getBotUsername() {
        return properties.getBotName();
    }

    @Override
    public String getBotToken() {
        return properties.getToken();
    }

    @Override
    public void onRegister() {
        setCommandList();
    }

    @Override
    public void onUpdateReceived(Update update) {
        onUpdatesReceived(Collections.singletonList(update));
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        updates.forEach(update -> {
            Message message = update.getMessage();
            if (message == null) {
                message = update.getChannelPost();
            } else {
                if (isNotBlank(message.getText())) {
                    eventPublisher.publishEvent(new CheckBotColumnResponseEvent(this, message.getText()));
                }
            }

            if (message != null) {
                Long chatId = message.getChatId();
                if (!chatId.equals(scanningChatId)) {
                    sendMessage("Permission access denied", chatId, false);
                    return;
                }
                Optional.ofNullable(message.getDocument()).ifPresent(document ->
                        downloadFile(document, chatId));
            }
        });
    }

    private void downloadFile(Document document, Long chatId) {
        if (document.getFileName().toLowerCase().endsWith(".xlsx") ||
                document.getFileName().toLowerCase().endsWith(".xls")) {
            String uploadedFileId = document.getFileId();
            GetFile uploadedFile = new GetFile();
            uploadedFile.setFileId(uploadedFileId);
            try {
                org.telegram.telegrambots.meta.api.objects.File file = execute(uploadedFile);
                File newFile = new File(uploadDir + "/" + System.currentTimeMillis() + "_" + document.getFileName());
                downloadFile(file, newFile);
                if (!fileInfoService.create(document, chatId, newFile, FileSource.TELEGRAM)) {
                    newFile.delete();
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public FileInfoEntity download(Long id) {
        FileInfoEntity fileInfo = fileInfoService.getById(id);
        GetFile uploadedFile = new GetFile();
        uploadedFile.setFileId(fileInfo.getTgFileId());
        try {
            org.telegram.telegrambots.meta.api.objects.File file = execute(uploadedFile);
            File newFile = new File(uploadDir + "/" + System.currentTimeMillis() + "_" + fileInfo.getName());
            downloadFile(file, newFile);
            fileInfo.setPath(newFile.getPath());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return fileInfo;
    }

    /*
    TODO
    1. увеличить минимальный размер файла
    2. ошибка INVALID_INN - необходимо исключать его и отправлять запроса снова
    3. хз, посмотреть , что все в относительно работчем состоянии и отправить на прод
     */

    public void sendMessage(String message, Long idChat, boolean withCancelButton) {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton("Отменить загрузку"));
        SendMessage method = SendMessage.builder()
                .chatId(String.valueOf(idChat == null ? scanningChatId : idChat))
                .parseMode(ParseMode.MARKDOWN)
                .text(message)
                .replyMarkup(!withCancelButton
                        ? null
                        : ReplyKeyboardMarkup.builder()
                        .resizeKeyboard(true)
                        .keyboard(Collections.singleton(keyboardRow))
                        .build())
                .build();
        executeCommand(method);
        log.info(transformTgMessage(message));
    }

    public void sendRequestGetColumnName(String fileName, List<String> requiredEmptyColumn, List<String> columnLines) {
        SendMessage method = SendMessage.builder()
                .chatId(String.valueOf(scanningChatId))
                .parseMode(ParseMode.MARKDOWN)
                .text(createRequestGetColumnNameMessage(fileName, requiredEmptyColumn, columnLines))
                .replyMarkup(ReplyKeyboardMarkup.builder()
                        .resizeKeyboard(true)
                        .keyboard(createRequestGetColumnNameMessageMessageKeyboard())
                        .build())
                .build();
        executeCommand(method);
    }

    public void sendRequestGetRequireColumn(FileInfoEntity file, String requireField) {
        SendMessage method = SendMessage.builder()
                .chatId(String.valueOf(scanningChatId))
                .parseMode(ParseMode.MARKDOWN)
                .text(createRequestGetRequireColumnNameMessage(file.getName(), requireField))
                .replyMarkup(ReplyKeyboardMarkup.builder()
                        .resizeKeyboard(true)
                        .keyboard(createRequestGetRequireColumnNameMessageMessageKeyboard(file))
                        .build())
                .build();
        executeCommand(method);
    }

    private static String createRequestGetColumnNameMessage(String fileName, List<String> requiredEmptyColumn,
                                                            List<String> columnLines) {
        return String.format("*Файл: %s*\n*Все еще не указаны обязательные поля*: _%s_\n\n" +
                        "*Какой это столбец?*\n_%s_", fileName,
                String.join(", ", requiredEmptyColumn),
                String.join("\n", columnLines));
    }

    private static String createRequestGetRequireColumnNameMessage(String fileName, String field) {
        return String.format("*Файл: %s*\n*" +
                "Не указано обязательное поле*: _%s_", fileName, field);
    }

    private static List<KeyboardRow> createRequestGetColumnNameMessageMessageKeyboard() {
        int buttonCountInRow = 3;
        List<KeyboardButton> buttons = Arrays.stream(XlsxRequireField.values())
                .filter(xlsxRequireField -> xlsxRequireField != XlsxRequireField.TRASH)
                .map(xlsxRequireField -> new KeyboardButton(xlsxRequireField.getDescription()))
                .sorted(Comparator.comparing(KeyboardButton::getText))
                .collect(Collectors.toList());
        buttons.add(new KeyboardButton("Пропустить"));
        buttons.add(new KeyboardButton("Отменить загрузку"));
        List<KeyboardRow> rows = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i = i + buttonCountInRow) {
            KeyboardRow row = new KeyboardRow();
            for (int j = i; j < Math.min(i + buttonCountInRow, buttons.size()); j++) {
                row.add(buttons.get(j));
            }
            rows.add(row);
        }
        return rows;
    }

    private static List<KeyboardRow> createRequestGetRequireColumnNameMessageMessageKeyboard(FileInfoEntity file) {
        int buttonCountInRow = 3;

        List<KeyboardButton> buttons = file.getColumnInfo().get().getColumnInfoMap().entrySet().stream()
                .map(entry -> new KeyboardButton(entry.getKey() + ". " + entry.getValue().stream().limit(2)
                        .collect(Collectors.joining("\n"))))
                .collect(Collectors.toList());

        buttons.add(new KeyboardButton("Пропустить"));
        buttons.add(new KeyboardButton("Отменить загрузку"));
        List<KeyboardRow> rows = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i = i + buttonCountInRow) {
            KeyboardRow row = new KeyboardRow();
            for (int j = i; j < Math.min(i + buttonCountInRow, buttons.size()); j++) {
                row.add(buttons.get(j));
            }
            rows.add(row);
        }
        return rows;
    }

    private void setCommandList() {
        SetMyCommands.SetMyCommandsBuilder commandsBuilder = SetMyCommands.builder();
        commandsBuilder.commands(Collections.singletonList(BotCommand.builder().command("/start")
                .description("/start").build()));
        executeCommand(commandsBuilder.build());
    }

    private void executeCommand(BotApiMethod<?> method) {
        try {
            execute(method);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            log.debug(e);
        }
    }


}
