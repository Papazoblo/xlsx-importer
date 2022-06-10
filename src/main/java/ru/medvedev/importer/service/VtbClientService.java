package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.client.VtbClient;
import ru.medvedev.importer.component.VtbProperties;
import ru.medvedev.importer.dto.LeadDto;
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
public class VtbClientService {

    private final VtbClient client;
    private final VtbProperties properties;

    public void login() {
        LoginRequest request = new LoginRequest();
        request.setGrantType("client_credentials");
        request.setApplicationId(properties.getClientId());
        request.setApplicationKey(properties.getClientKey());

        LoginResponse response = client.login(URI.create(properties.getTokenUrl()), request);
        properties.setAccessToken(response.getAccessToken());
    }

    public void createLead(LeadDto leadDto) {
        LeadRequest request = new LeadRequest();
        request.setLeads(Collections.singletonList(leadDto));
        client.addLead(request);
    }

    public List<LeadInfoResponse> checkLead(List<String> innList) {
        List<LeadDto> leads = innList.stream().map(inn -> {
            LeadDto lead = new LeadDto();
            lead.setInn(inn);
            return lead;
        }).collect(Collectors.toList());
        LeadRequest leadRequest = new LeadRequest();
        leadRequest.setLeads(leads);
        CheckLeadResponse response = client.checkLeads(leadRequest);
        return response.getLeads().stream().filter(item -> item.getResponseCode() == POSITIVE)
                .collect(Collectors.toList());
    }
}
