package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.client.VtbClient;
import ru.medvedev.importer.component.VtbProperties;
import ru.medvedev.importer.dto.LeadDto;
import ru.medvedev.importer.dto.WebhookLeadDto;
import ru.medvedev.importer.dto.request.LeadRequest;
import ru.medvedev.importer.dto.request.LoginRequest;
import ru.medvedev.importer.dto.response.CheckLeadResponse;
import ru.medvedev.importer.dto.response.LeadInfoResponse;
import ru.medvedev.importer.dto.response.LoginResponse;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.medvedev.importer.enums.CheckLeadStatus.POSITIVE;

@Service
@RequiredArgsConstructor
@Slf4j
public class VtbClientService {

    private static final String BEARER = "Bearer ";

    private final VtbClient client;
    private final VtbProperties properties;

    public void login() {
        LoginRequest request = new LoginRequest();
        request.setGrant_type("client_credentials");
        request.setClient_id(properties.getClientId());
        request.setClient_secret(properties.getClientSecret());

        LoginResponse response = client.login(URI.create(properties.getTokenUrl()), request);
        properties.setAccessToken(response.getAccessToken());
    }

    public void createLead(WebhookLeadDto webhookLead) {

        log.debug("*** Create lead in VTB");

        login();
        LeadDto leadDto = new LeadDto();
        leadDto.setCity(webhookLead.getCity());
        leadDto.setInn(webhookLead.getInn());
        leadDto.setPhone(webhookLead.getPhones());
        leadDto.setConsentOnPersonalDataProcessing(true);
        LeadRequest request = new LeadRequest();
        request.setLeads(Collections.singletonList(leadDto));
        try {
            client.addLead(request, BEARER + properties.getAccessToken());
        } catch (Exception ex) {
            log.debug("*** ошибка добавления лида", ex);
        }
    }

    public List<LeadInfoResponse> checkLead(List<String> innList) {

        log.debug("*** Check duplicate in VTB {}", innList.size());

        List<LeadDto> leads = innList.stream().map(inn -> {
            LeadDto lead = new LeadDto();
            lead.setInn(inn);
            return lead;
        }).collect(Collectors.toList());
        LeadRequest leadRequest = new LeadRequest();
        leadRequest.setLeads(leads);
        CheckLeadResponse response = client.checkLeads(leadRequest, BEARER + properties.getAccessToken());
        return response.getLeads().stream().filter(item -> item.getResponseCode() == POSITIVE)
                .collect(Collectors.toList());
    }
}
