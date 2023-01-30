package ru.medvedev.importer.service.telegram.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Log4j2
public class TelegramNotificatorConnector {

    private final TelegramNotificatorPollingService pollingService;

    @PostConstruct
    public void connect() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(TelegramNotificatorBotSession.class);
            telegramBotsApi.registerBot(pollingService);
        } catch (TelegramApiException ex) {
            log.debug(ex);
        }
    }
}
