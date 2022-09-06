package ru.medvedev.importer.service.telegram.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.medvedev.importer.component.TelegramNotificatorProperty;
import ru.medvedev.importer.service.NotificationChatService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static ru.medvedev.importer.utils.StringUtils.transformTgMessage;


@RequiredArgsConstructor
@Service
@Log4j2
public class TelegramNotificatorPollingService extends TelegramLongPollingBot {

    private final TelegramNotificatorProperty properties;
    private final NotificationChatService notificationChatService;

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

            if (isNotBlank(message.getText()) && message.getText().startsWith("/start")) {
                String[] splitText = message.getText().trim().split(" ");
                if (splitText.length == 2) {
                    try {
                        if (LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd:HH")).equals(splitText[1])) {
                            notificationChatService.addChatId(message.getChatId());
                            sendMessage("Авторизация прошла успешно", message.getChatId());
                        } else {
                            sendMessage("Невозможно авторизироваться", message.getChatId());
                        }
                    } catch (Exception ex) {
                        log.debug("*** error send telegram notification", ex);
                        sendMessage("Невозможно авторизироваться", message.getChatId());
                    }
                }
            }
        });
    }

    public void sendMessage(String message, Long idChat) {
        SendMessage method = SendMessage.builder()
                .chatId(idChat.toString())
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
