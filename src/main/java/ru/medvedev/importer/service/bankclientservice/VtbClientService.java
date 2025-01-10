package ru.medvedev.importer.service.bankclientservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.client.VtbApiClient;
import ru.medvedev.importer.component.VtbProperties;
import ru.medvedev.importer.dto.CheckLeadResult;
import ru.medvedev.importer.dto.CreateLeadResult;
import ru.medvedev.importer.dto.LeadDto;
import ru.medvedev.importer.dto.WebhookLeadDto;
import ru.medvedev.importer.dto.request.LeadRequest;
import ru.medvedev.importer.dto.response.CheckLeadBadRequestResponse;
import ru.medvedev.importer.dto.response.CheckLeadResponse;
import ru.medvedev.importer.entity.WebhookStatisticEntity;
import ru.medvedev.importer.entity.WebhookStatusEntity;
import ru.medvedev.importer.exception.ErrorCheckLeadException;
import ru.medvedev.importer.exception.ErrorCreateLeadException;
import ru.medvedev.importer.exception.TimeOutException;
import ru.medvedev.importer.service.BankCountSendedRequestService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.medvedev.importer.enums.Bank.VTB;

@Service
@RequiredArgsConstructor
@Slf4j
public class VtbClientService implements BankClientService {

    private static final String BEARER = "Bearer ";

    private final BankCountSendedRequestService bankCountSendedRequestService;
    private final VtbApiClient client;
    private final VtbProperties properties;


    public CreateLeadResult createLead(WebhookLeadDto webhookLead, WebhookStatusEntity webhookStatusEntity) {

        log.debug("*** Create lead in VTB");

        LeadDto leadDto = new LeadDto();
        leadDto.setCity(webhookLead.getCity());
        leadDto.setInn(webhookLead.getInn());
        leadDto.setPhone(webhookLead.getPhones());
        leadDto.setConsentOnPersonalDataProcessing(true);
        LeadRequest request = new LeadRequest();
        request.setLeads(Collections.singletonList(leadDto));
        try {
            bankCountSendedRequestService.incCreateRequestCount(VTB, 1);
            client.addLead(request, BEARER + properties.getAccessToken());
            return CreateLeadResult.of(true);
        } catch (TimeOutException | ErrorCreateLeadException ex) {
            throw ex;
        } catch (Exception ex) {
            log.debug("*** ошибка добавления лида " + ex.getMessage(), ex);
            return CreateLeadResult.of(false);
        }
    }

    public CheckLeadResult getAllFromCheckLead(WebhookStatisticEntity webhookStatisticEntity, List<String> innList, Long fileId) {

        log.debug("*** Check duplicate in Bank");

        List<LeadDto> leads = innList.stream().map(inn -> {
            LeadDto lead = new LeadDto();
            lead.setInn(inn);
            return lead;
        }).collect(Collectors.toList());
        LeadRequest leadRequest = new LeadRequest();
        leadRequest.setLeads(leads);
        try {
            bankCountSendedRequestService.incCheckRequestCount(VTB, 1);
            ResponseEntity<CheckLeadResponse> response = client.checkLeads(leadRequest,
                    BEARER + properties.getAccessToken());
            return CheckLeadResult.of(true, null, response.getBody().getLeads());
        } catch (TimeOutException ex) {
            throw ex;
        } catch (Exception ex) {
            CheckLeadBadRequestResponse responseBody = new CheckLeadBadRequestResponse();
            responseBody.setMoreInformation("Ошибка проверки на дубликат");
            if (ex instanceof RetryableException) {
                try {
                    responseBody = new ObjectMapper().readValue(
                            ((RetryableException) ex).responseBody().map(body -> new String(body.array())).orElse(""),
                            CheckLeadBadRequestResponse.class);
                } catch (IOException ioEx) {
                    responseBody.setMoreInformation("Ошибка проверки на дубликат");
                }
            }
            throw new ErrorCheckLeadException(responseBody.getMoreInformation(), fileId);
        }
    }
}
