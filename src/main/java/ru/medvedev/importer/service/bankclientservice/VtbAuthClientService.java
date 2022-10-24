package ru.medvedev.importer.service.bankclientservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.client.VtbAuthClient;
import ru.medvedev.importer.component.VtbProperties;
import ru.medvedev.importer.dto.request.LoginRequest;
import ru.medvedev.importer.dto.response.LoginResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class VtbAuthClientService {

    private final VtbAuthClient client;
    private final VtbProperties properties;

    public String login() {
        LoginRequest request = new LoginRequest();
        request.setGrant_type("client_credentials");
        request.setClient_id(String.format("%s%s", properties.getClientId(), properties.getDomen()));
        request.setClient_secret(properties.getClientSecret());

        LoginResponse response = client.login(request);
        properties.setAccessToken(response.getAccessToken());
        return response.getAccessToken();
    }
}
