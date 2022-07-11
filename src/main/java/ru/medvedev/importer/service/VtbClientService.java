package ru.medvedev.importer.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.client.VtbApiClient;
import ru.medvedev.importer.component.VtbProperties;
import ru.medvedev.importer.component.XlsxStorage;
import ru.medvedev.importer.dto.LeadDto;
import ru.medvedev.importer.dto.WebhookLeadDto;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.dto.request.LeadRequest;
import ru.medvedev.importer.dto.response.CheckLeadResponse;
import ru.medvedev.importer.dto.response.LeadInfoResponse;
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
    private final XlsxStorage xlsxStorage;
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

    public List<LeadInfoResponse> getPositiveFromCheckLead(List<String> innList) {
        return getAllFromCheckLead(innList).stream().filter(item -> item.getResponseCode() == POSITIVE)
                .collect(Collectors.toList());
    }

    public List<LeadInfoResponse> getAllFromCheckLead(List<String> innList) {

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
        } catch (FeignException.FeignClientException ex) {
            log.debug("*** Error check duplicate: {}", ex.getMessage(), ex);
            eventPublisher.publishEvent(new ImportEvent(this, "Ошибка проверки дубликатов ВТБ. Http статус " +
                    ex.status(),
                    EventType.LOG, xlsxStorage.getFileId()));
            return Collections.emptyList();
        }
    }
}
