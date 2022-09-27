package ru.medvedev.importer.service.telegram.xlsxcollector;

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
import ru.medvedev.importer.component.TelegramXlsxCollectorProperty;
import ru.medvedev.importer.dto.events.BankSelectResponseEvent;
import ru.medvedev.importer.dto.events.CheckBotColumnResponseEvent;
import ru.medvedev.importer.dto.events.ProjectCodeResponseEvent;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.ChatState;
import ru.medvedev.importer.enums.FileSource;
import ru.medvedev.importer.enums.SystemVariable;
import ru.medvedev.importer.enums.XlsxRequireField;
import ru.medvedev.importer.service.FileInfoService;
import ru.medvedev.importer.service.SystemVariableService;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static ru.medvedev.importer.enums.ChatState.BANK_SELECT;
import static ru.medvedev.importer.enums.ChatState.PROJECT_CODE;
import static ru.medvedev.importer.utils.StringUtils.transformTgMessage;


@RequiredArgsConstructor
@Service
@Log4j2
public class TelegramPollingService extends TelegramLongPollingBot {

    @Value("${directory.upload-dir}")
    private String uploadDir;
    @Value("${telegram.xlsx-collector.scanningChatId}")
    private Long scanningChatId;

    private final TelegramXlsxCollectorProperty properties;
    private final FileInfoService fileInfoService;
    private final ApplicationEventPublisher eventPublisher;
    private final SystemVariableService systemVariableService;

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
                    String messageText = message.getText();
                    systemVariableService.getByCode(SystemVariable.CHAT_STATE).ifPresent(entity -> {
                        ChatState chatState = ChatState.valueOf(entity.getValue());
                        if (chatState == ChatState.COLUMN_NAME) {
                            eventPublisher.publishEvent(new CheckBotColumnResponseEvent(this, messageText));
                        } else if (chatState == PROJECT_CODE) {
                            eventPublisher.publishEvent(new ProjectCodeResponseEvent(this, messageText));
                        } else if (chatState == BANK_SELECT) {
                            eventPublisher.publishEvent(new BankSelectResponseEvent(this, messageText));
                        }
                    });
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

    public void sendRequestToSelectBank(String fileName, List<String> buttons) {
        SendMessage method = SendMessage.builder()
                .chatId(String.valueOf(scanningChatId))
                .parseMode(ParseMode.MARKDOWN)
                .text(selectBankRequestMessage(fileName))
                .replyMarkup(ReplyKeyboardMarkup.builder()
                        .resizeKeyboard(true)
                        .keyboard(createRequestGetColumnNameMessageKeyboard(buttons, 3))
                        .build())
                .build();
        executeCommand(method);
    }

    public void sendRequestGetProjectCode(String bankName, String fileName, List<String> buttons) {
        SendMessage method = SendMessage.builder()
                .chatId(String.valueOf(scanningChatId))
                .parseMode(ParseMode.MARKDOWN)
                .text(createGetTgGetProjectCodeMessage(bankName, fileName))
                .replyMarkup(ReplyKeyboardMarkup.builder()
                        .resizeKeyboard(true)
                        .keyboard(createRequestGetColumnNameMessageKeyboard(buttons, 1))
                        .build())
                .build();
        executeCommand(method);
    }

    public void sendRequestGetColumnName(String fileName, List<String> requiredEmptyColumn, List<String> columnLines) {
        SendMessage method = SendMessage.builder()
                .chatId(String.valueOf(scanningChatId))
                .parseMode(ParseMode.MARKDOWN)
                .text(createRequestGetColumnNameMessage(fileName, requiredEmptyColumn, columnLines))
                .replyMarkup(ReplyKeyboardMarkup.builder()
                        .resizeKeyboard(true)
                        .keyboard(createRequestGetColumnNameMessageKeyboard())
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
        return String.format("Файл: `%s`\nВсе еще не указаны обязательные поля: `%s`\n\n" +
                        "Какой это столбец?\n`%s`", fileName,
                String.join(", ", requiredEmptyColumn),
                String.join("\n", columnLines));
    }

    private static String createGetTgGetProjectCodeMessage(String bankName, String fileName) {
        return String.format("Файл: `%s`\nВыберите проект для загрузки в `%s` или укажите другой", fileName, bankName);
    }

    private static String selectBankRequestMessage(String fileName) {
        return String.format("Файл: `%s`\nВыберите банк для загрузки контактов", fileName);
    }

    private static String createRequestGetRequireColumnNameMessage(String fileName, String field) {
        return String.format("Файл: `%s`\n" +
                "Не указано обязательное поле: `%s`", fileName, field);
    }

    private static List<KeyboardRow> createRequestGetColumnNameMessageKeyboard(List<String> buttons, int columnCount) {
        buttons.add("Отменить загрузку");
        return buildKeyboard(buttons, columnCount);
    }

    private static List<KeyboardRow> createRequestGetColumnNameMessageKeyboard() {
        List<String> buttons = Arrays.stream(XlsxRequireField.values())
                .filter(xlsxRequireField -> xlsxRequireField != XlsxRequireField.TRASH)
                .map(XlsxRequireField::getDescription)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        buttons.add("Пропустить");
        buttons.add("Отменить загрузку");
        return buildKeyboard(buttons, 3);
    }

    private static List<KeyboardRow> buildKeyboard(List<String> buttonNames, int columnCount) {
        List<KeyboardRow> rows = new ArrayList<>();
        for (int i = 0; i < buttonNames.size(); i = i + columnCount) {
            KeyboardRow row = new KeyboardRow();
            for (int j = i; j < Math.min(i + columnCount, buttonNames.size()); j++) {
                row.add(buttonNames.get(j));
            }
            rows.add(row);
        }
        return rows;
    }

    private static List<KeyboardRow> createRequestGetRequireColumnNameMessageMessageKeyboard(FileInfoEntity file) {
        List<String> buttons = file.getColumnInfo().get().getColumnInfoMap().entrySet().stream()
                .map(entry -> String.format("%s. %s", entry.getKey(), entry.getValue().stream().limit(2)
                        .collect(Collectors.joining("\n"))))
                .collect(Collectors.toList());

        buttons.add("Пропустить");
        buttons.add("Отменить загрузку");
        return buildKeyboard(buttons, 3);
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
