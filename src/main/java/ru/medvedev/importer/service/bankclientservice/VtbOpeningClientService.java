package ru.medvedev.importer.service.bankclientservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.client.VtbOpeningApiClient;
import ru.medvedev.importer.dto.CheckLeadResult;
import ru.medvedev.importer.dto.CreateLeadResult;
import ru.medvedev.importer.dto.WebhookLeadDto;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.dto.request.VtbOpeningCheckInn;
import ru.medvedev.importer.dto.request.VtbOpeningCreateLead;
import ru.medvedev.importer.dto.response.LeadInfoResponse;
import ru.medvedev.importer.dto.response.VtbOpeningCheckInnResponse;
import ru.medvedev.importer.dto.response.VtbOpeningCheckResultResponse;
import ru.medvedev.importer.dto.response.VtbOpeningResponse;
import ru.medvedev.importer.enums.EventType;
import ru.medvedev.importer.enums.VtbOpeningInnStatus;
import ru.medvedev.importer.enums.VtbOpeningRequestStatus;
import ru.medvedev.importer.exception.ErrorCheckLeadException;
import ru.medvedev.importer.exception.ErrorCreateVtbLeadException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static ru.medvedev.importer.enums.CheckLeadStatus.*;
import static ru.medvedev.importer.enums.VtbOpeningRequestStatus.created;
import static ru.medvedev.importer.enums.VtbOpeningRequestStatus.exported;

@Service
@RequiredArgsConstructor
@Slf4j
public class VtbOpeningClientService implements BankClientService {

    private final VtbOpeningApiClient client;
    private final ApplicationEventPublisher eventPublisher;

    public CreateLeadResult createLead(WebhookLeadDto webhookLead) {

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
        lead.setFull_name(webhookLead.getName());

        try {
            ResponseEntity<VtbOpeningResponse> response = client.creteAddLeadRequest(lead);
            if (response.getStatusCode() == HttpStatus.OK) {
                String id = response.getBody().getId();
                log.debug("*** createLeadRequest id = {}", id);
                return CreateLeadResult.of(true, id);
            } else {
                log.debug("*** VtbOpening error create lead [{}]", response.getStatusCode());
                return CreateLeadResult.of(false);
            }
        } catch (ErrorCreateVtbLeadException ex) {
            throw ex;
        } catch (Exception ex) {
            log.debug("*** ошибка добавления лида " + ex.getMessage(), ex);
            return CreateLeadResult.of(false);
        }
    }

    @Override
    public CheckLeadResult getAllFromCheckLead(List<String> innList, Long fileId) {

        log.debug("*** Check duplicate in VTB Opening");

        VtbOpeningCheckInn request = new VtbOpeningCheckInn();
        request.setInns(innList);
        try {
            ResponseEntity<VtbOpeningResponse> response = client.createCheckLeadsRequest(request);
            if (response.getStatusCode() == HttpStatus.OK) {
                String id = response.getBody().getId();
                return CheckLeadResult.of(true, id, innList.stream()
                        .map(item -> {
                            LeadInfoResponse info = new LeadInfoResponse();
                            info.setInn(item);
                            info.setResponseCode(IN_CHECK);
                            return info;
                        })
                        .collect(toList()));
            } else {
                log.debug("*** VtbOpening error check lead status [{}]", response.getStatusCode());
                return CheckLeadResult.of(false, "blank", Collections.emptyList());
            }
        } catch (Exception ex) {
            log.debug("*** Error check duplicate: {} {}", ex.getMessage(), ex);
            eventPublisher.publishEvent(new ImportEvent(this, "Ошибка проверки дубликатов ВТБ Открытие. " +
                    ex.getMessage(),
                    EventType.LOG, fileId));
            return CheckLeadResult.of(false, "blank", Collections.emptyList());
        }
    }

    @Override
    public CheckLeadResult getCheckLeadResult(String id, Long fileId) {

        ResponseEntity<VtbOpeningCheckInnResponse> response = client.getCheckLeadsResponse(id);
        if (response.getStatusCode() == HttpStatus.OK) {
            VtbOpeningCheckInnResponse result = response.getBody();
            if (result.getStatus() == VtbOpeningRequestStatus.error) {
                throw new ErrorCheckLeadException(result.getMessage(), fileId);
            } else if (result.getStatus() == VtbOpeningRequestStatus.done) {
                return CheckLeadResult.of(true, null, result.getResult().getInns().stream()
                        .map(item -> {
                            LeadInfoResponse leadInfo = new LeadInfoResponse();
                            leadInfo.setInn(item.getInn());
                            leadInfo.setResponseCode(item.getInnStatus() == VtbOpeningInnStatus.success
                                    ? POSITIVE : NEGATIVE);
                            return leadInfo;
                        }).collect(toList()));
            }
        }
        return CheckLeadResult.of(false);
    }

    @Override
    public boolean getCreateLeadResult(String id) {
        ResponseEntity<VtbOpeningCheckResultResponse> response = client.getAddLeadResponse(id);
        if (response.getStatusCode() == HttpStatus.OK) {
            VtbOpeningCheckResultResponse result = response.getBody();
            if (result.getStatus().equals("new") ||
                    VtbOpeningRequestStatus.valueOf(result.getStatus()) == VtbOpeningRequestStatus.inqueue) {
                return false;
            } else if (Stream.of(exported, created).anyMatch(status ->
                    status == VtbOpeningRequestStatus.valueOf(result.getStatus()))) {
                return true;
            } else {
                throw new ErrorCreateVtbLeadException(result.getLabel() + ". Статус заявки: `" + result.getStatus() + "`", -1L);
            }
        } else {
            log.debug("*** error thread sleep VtbOpeningClientService::getCreateLeadResult [{}]", response.getStatusCode());
            return false;
        }
    }
}
