package ru.medvedev.importer.service.bankclientservice;

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
import ru.medvedev.importer.enums.EventType;
import ru.medvedev.importer.exception.ErrorCreateVtbLeadException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VtbClientService implements BankClientService {

    private static final String BEARER = "Bearer ";

    private final VtbApiClient client;
    private final VtbProperties properties;
    private final ApplicationEventPublisher eventPublisher;


    public boolean createLead(WebhookLeadDto webhookLead) {

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
            return true;
        } catch (ErrorCreateVtbLeadException ex) {
            throw ex;
        } catch (Exception ex) {
            log.debug("*** ошибка добавления лида " + ex.getMessage(), ex);
            return false;
        }
    }

    public List<LeadInfoResponse> getAllFromCheckLead(List<String> innList, Long fileId) {

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
                    EventType.LOG, fileId));
            return Collections.emptyList();
        }
    }
}
