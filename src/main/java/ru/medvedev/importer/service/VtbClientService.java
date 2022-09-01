package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.client.VtbApiClient;
import ru.medvedev.importer.component.VtbProperties;
import ru.medvedev.importer.dto.LeadDto;
import ru.medvedev.importer.dto.WebhookLeadDto;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.dto.request.LeadRequest;
import ru.medvedev.importer.dto.response.CheckLeadResponse;
import ru.medvedev.importer.dto.response.LeadInfoResponse;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.EventType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.medvedev.importer.enums.CheckLeadStatus.POSITIVE;

@Service
@RequiredArgsConstructor
@Slf4j
public class VtbClientService {

    private static final String BEARER = "Bearer ";

    private final VtbApiClient client;
    private final VtbProperties properties;
    private final ApplicationEventPublisher eventPublisher;


    public void createLead(WebhookLeadDto webhookLead) {

        log.debug("*** Create lead in VTB");

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
            log.debug("*** ошибка добавления лида " + ex.getMessage(), ex);
        }
    }

    public List<LeadInfoResponse> getPositiveFromCheckLead(List<String> innList, FileInfoEntity fileInfo) {
        return getAllFromCheckLead(innList, fileInfo).stream().filter(item -> item.getResponseCode() == POSITIVE)
                .collect(Collectors.toList());
    }

    public List<LeadInfoResponse> getAllFromCheckLead(List<String> innList, FileInfoEntity fileInfo) {

        log.debug("*** Check duplicate in VTB");

        List<LeadDto> leads = innList.stream().map(inn -> {
            LeadDto lead = new LeadDto();
            lead.setInn(inn);
            return lead;
        }).collect(Collectors.toList());
        LeadRequest leadRequest = new LeadRequest();
        leadRequest.setLeads(leads);
        try {
            ResponseEntity<CheckLeadResponse> response = client.checkLeads(leadRequest,
                    BEARER + properties.getAccessToken());
            return response.getBody().getLeads();
        } catch (Exception ex) {
            log.debug("*** Error check duplicate: {} {}", ex.getMessage(), ex);
            eventPublisher.publishEvent(new ImportEvent(this, "Ошибка проверки дубликатов ВТБ. " +
                    ex.getMessage(),
                    EventType.LOG, fileInfo == null ? -1L : fileInfo.getId()));
            return Collections.emptyList();
        }
    }
}
