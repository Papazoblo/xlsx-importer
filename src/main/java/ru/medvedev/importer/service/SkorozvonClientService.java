package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.client.SkorozvonClient;
import ru.medvedev.importer.config.SkorozvonProperties;
import ru.medvedev.importer.dto.request.LoginRequest;
import ru.medvedev.importer.dto.request.RefreshRequest;
import ru.medvedev.importer.dto.response.LoginResponse;

@Service
@RequiredArgsConstructor
public class SkorozvonClientService {

    private static final String BEARER = "Bearer ";
    private final SkorozvonClient client;
    private final SkorozvonProperties properties;

    private void login() {
        LoginRequest request = new LoginRequest();
        request.setApiKey(properties.getApiKey());
        request.setApplicationId(properties.getApplicationId());
        request.setApplicationKey(properties.getApplicationKey());
        request.setUsername(properties.getLogin());
        LoginResponse response = client.login(request);
        properties.setAccessToken(response.getAccessToken());
        properties.setRefreshToken(response.getRefreshToken());
    }

    private void refreshToken() {
        RefreshRequest request = new RefreshRequest();
        request.setApplicationId(properties.getApplicationId());
        request.setApplicationKey(properties.getApplicationKey());
        request.setRefreshToken(properties.getRefreshToken());
        LoginResponse response = client.refresh(request, BEARER + properties.getAccessToken());
        properties.setAccessToken(response.getAccessToken());
        properties.setRefreshToken(response.getRefreshToken());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void apiLogin() {
        login();
    }

    @Scheduled(fixedRateString = "${skorozvon.fixed-rate-update-token}", initialDelay = 3600000)
    public void updateTokenScheduler() {
        refreshToken();
    }
}
