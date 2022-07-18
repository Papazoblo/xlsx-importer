package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.client.SkorozvonAuthClient;
import ru.medvedev.importer.component.SkorozvonProperties;
import ru.medvedev.importer.dto.request.LoginRequest;
import ru.medvedev.importer.dto.request.RefreshRequest;
import ru.medvedev.importer.dto.response.LoginResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkorozvonAuthClientService {

    private static final String BEARER = "Bearer ";
    private final SkorozvonAuthClient client;
    private final SkorozvonProperties properties;

    private String login() {
        LoginRequest request = new LoginRequest();
        request.setApiKey(properties.getApiKey());
        request.setClient_id(properties.getApplicationId());
        request.setClient_secret(properties.getApplicationKey());
        request.setUsername(properties.getLogin());
        LoginResponse response = client.login(request);
        properties.setAccessToken(response.getAccessToken());
        properties.setRefreshToken(response.getRefreshToken());
        return properties.getAccessToken();
    }

    public void refreshToken() {
        log.debug("*** refreshing token");
        RefreshRequest request = new RefreshRequest();
        request.setApplicationId(properties.getApplicationId());
        request.setApplicationKey(properties.getApplicationKey());
        request.setRefreshToken(properties.getRefreshToken());
        LoginResponse response = client.refresh(request, BEARER + properties.getAccessToken());
        properties.setAccessToken(response.getAccessToken());
        properties.setRefreshToken(response.getRefreshToken());
    }

    @EventListener(ApplicationReadyEvent.class)
    public String apiLogin() {
        return login();
    }

    @Scheduled(fixedRateString = "${cron.fixed-rate-update-token}", initialDelay = 10000)
    public void updateTokenScheduler() {
        refreshToken();
    }
}
