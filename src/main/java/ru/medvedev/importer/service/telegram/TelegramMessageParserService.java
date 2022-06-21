package ru.medvedev.importer.service.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.medvedev.importer.service.telegram.handler.BaseHandler;

@Service
@RequiredArgsConstructor
public class TelegramMessageParserService {

    private static final String BOT_NAME_DELIMITER = "@";

    public BaseHandler parseMessage(Message message) {
        String commandLine = getCommandWithoutBotName(message.getText());
        return getHandlerForAuthenticated(commandLine);
    }

    private BaseHandler getHandlerForAuthenticated(String commandLine) {
        String command = getCommandName(commandLine);
        BaseHandler handler = (message, messageSender) -> {

        };
        return handler;
    }

    private static String getCommandWithoutBotName(String command) {
        return command.split(BOT_NAME_DELIMITER)[0].trim();
    }

    private static String getCommandName(String line) {
        String[] array = line.split(" ");
        if (array.length != 0) {
            return array[0];
        } else {
            return "";
        }
    }

}
