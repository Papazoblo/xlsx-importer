package ru.medvedev.importer.service.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.medvedev.importer.component.TelegramProperty;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.service.FileInfoService;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
            }
            if (message != null) {
                Long chatId = message.getChatId();
                if (!chatId.equals(scanningChatId)) {
                    sendMessage("Permission access denied", chatId);
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
                if (!fileInfoService.create(document, chatId, newFile)) {
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

    public void sendMessage(String message, Long idChat) {
        SendMessage method = SendMessage.builder()
                .chatId(String.valueOf(idChat))
                .parseMode(ParseMode.MARKDOWN)
                .text(message)
                .build();
        executeCommand(method);
        log.info(transformTgMessage(message));
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
