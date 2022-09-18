package ru.medvedev.importer.service.bankclientservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.client.VtbOpeningApiClient;
import ru.medvedev.importer.dto.WebhookLeadDto;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.dto.request.VtbOpeningCheckInn;
import ru.medvedev.importer.dto.request.VtbOpeningCreateLead;
import ru.medvedev.importer.dto.response.*;
import ru.medvedev.importer.enums.EventType;
import ru.medvedev.importer.enums.VtbOpeningInnStatus;
import ru.medvedev.importer.enums.VtbOpeningRequestStatus;
import ru.medvedev.importer.exception.ErrorCreateVtbLeadException;
import ru.medvedev.importer.exception.FileProcessingException;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static ru.medvedev.importer.enums.CheckLeadStatus.NEGATIVE;
import static ru.medvedev.importer.enums.CheckLeadStatus.POSITIVE;

@Service
@RequiredArgsConstructor
@Slf4j
public class VtbOpeningClientService implements BankClientService {

    private final VtbOpeningApiClient client;
    private final ApplicationEventPublisher eventPublisher;

    public boolean createLead(WebhookLeadDto webhookLead) {

        log.debug("*** Create lead in VTB Opening");

        VtbOpeningCreateLead lead = new VtbOpeningCreateLead();
        lead.setCity(webhookLead.getCity());
        lead.setInn(webhookLead.getInn());
        lead.setPhone(webhookLead.getPhones());
        if (webhookLead.getEmails().isEmpty()) {
            lead.setEmail("1@ya.ru");
        } else {
            lead.setEmail(webhookLead.getEmails().get(0));
        }
        lead.setFullName(webhookLead.getName());

        try {
            ResponseEntity<VtbOpeningResponse> response = client.creteAddLeadRequest(lead);
            if (response.getStatusCode() == HttpStatus.OK) {
                String id = response.getBody().getId();
                log.debug("*** createLeadRequest id = {}", id);
                return getCreateLeadResult(id);
            } else {
                log.debug("*** VtbOpening error check lead status [{}]", response.getStatusCode());
            }
            return true;
        } catch (ErrorCreateVtbLeadException ex) {
            throw ex;
        } catch (Exception ex) {
            log.debug("*** ошибка добавления лида " + ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public List<LeadInfoResponse> getAllFromCheckLead(List<String> innList, Long fileId) {

        log.debug("*** Check duplicate in VTB Opening");

        VtbOpeningCheckInn request = new VtbOpeningCheckInn();
        request.setInns(innList);
        try {
            ResponseEntity<VtbOpeningResponse> response = client.createCheckLeadsRequest(request);
            if (response.getStatusCode() == HttpStatus.OK) {
                String id = response.getBody().getId();
                log.debug("*** checkInnRequest id = {}", id);
                return getCheckLeadResult(id, fileId).stream()
                        .map(item -> {
                            LeadInfoResponse leadInfo = new LeadInfoResponse();
                            leadInfo.setInn(item.getInn());
                            leadInfo.setResponseCode(item.getInnStatus() == VtbOpeningInnStatus.success
                                    ? POSITIVE : NEGATIVE);
                            return leadInfo;
                        }).collect(toList());
            } else {
                log.debug("*** VtbOpening error check lead status [{}]", response.getStatusCode());
                return Collections.emptyList();
            }
        } catch (Exception ex) {
            log.debug("*** Error check duplicate: {} {}", ex.getMessage(), ex);
            eventPublisher.publishEvent(new ImportEvent(this, "Ошибка проверки дубликатов ВТБ Открытие. " +
                    ex.getMessage(),
                    EventType.LOG, fileId));
            return Collections.emptyList();
        }
    }

    private List<VtbOpeningCheckInnInfo> getCheckLeadResult(String id, Long fileId) {

        for (; ; ) {
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                log.debug("*** error thread sleep VtbOpeningClientService::getCheckLeadResult");
            }
            ResponseEntity<VtbOpeningCheckInnResponse> response = client.getCheckLeadsResponse(id);
            if (response.getStatusCode() == HttpStatus.OK) {
                VtbOpeningCheckInnResponse result = response.getBody();
                if (result.getStatus() == VtbOpeningRequestStatus.error) {
                    throw new FileProcessingException(result.getMessage(), fileId);
                } else if (result.getStatus() == VtbOpeningRequestStatus.done) {
                    return result.getResult().getInns();
                }
            } else {
                log.debug("*** error thread sleep VtbOpeningClientService::getCheckLeadResult [{}]", response.getStatusCode());
            }
        }
    }

    private boolean getCreateLeadResult(String id) {

        for (; ; ) {
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                log.debug("*** error thread sleep VtbOpeningClientService::getCreateLeadResult");
            }
            ResponseEntity<VtbOpeningCheckResultResponse> response = client.getAddLeadResponse(id);
            if (response.getStatusCode() == HttpStatus.OK) {
                VtbOpeningCheckResultResponse result = response.getBody();
                if (result.getStatus().equals("new") || VtbOpeningRequestStatus.valueOf(result.getStatus()) == VtbOpeningRequestStatus.inqueue) {
                    continue;
                } else if (VtbOpeningRequestStatus.valueOf(result.getStatus()) == VtbOpeningRequestStatus.created) {
                    return true;
                } else {
                    throw new ErrorCreateVtbLeadException(result.getLabel(), -1L);
                }
            } else {
                log.debug("*** error thread sleep VtbOpeningClientService::getCreateLeadResult [{}]", response.getStatusCode());
            }
        }
    }
}
