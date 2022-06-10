package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.client.SkorozvonClient;
import ru.medvedev.importer.component.SkorozvonProperties;
import ru.medvedev.importer.dto.CreateLeadDto;
import ru.medvedev.importer.dto.CreateOrganizationDto;
import ru.medvedev.importer.dto.request.CreateMultipleRequest;
import ru.medvedev.importer.dto.request.ImportLeadRequest;
import ru.medvedev.importer.dto.request.LoginRequest;
import ru.medvedev.importer.dto.request.RefreshRequest;
import ru.medvedev.importer.dto.response.LoginResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkorozvonClientService {

    private static final String BEARER = "Bearer ";
    private final SkorozvonClient client;
    private final SkorozvonProperties properties;

    public void importLeads(Long projectId, List<CreateLeadDto> leads, List<String> tags) {
        ImportLeadRequest request = new ImportLeadRequest();
        request.setCallProjectId(projectId);
        request.setData(leads);
        request.setTags(tags);
        client.importLeads(request, BEARER + properties.getAccessToken());
    }

    public void createMultiple(Long projectId, List<CreateOrganizationDto> leads, List<String> tags) {
        CreateMultipleRequest request = new CreateMultipleRequest();
        request.setCallProjectId(projectId);
        request.setData(leads);
        request.setTags(tags);
        client.createMultipleLeads(request, BEARER + properties.getAccessToken());
    }

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
