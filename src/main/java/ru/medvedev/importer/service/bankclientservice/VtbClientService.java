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
import ru.medvedev.importer.exception.ErrorCheckLeadException;
import ru.medvedev.importer.exception.ErrorCreateVtbLeadException;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
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


    public CreateLeadResult createLead(WebhookLeadDto webhookLead) {

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
            return CreateLeadResult.of(true);
        } catch (ErrorCreateVtbLeadException ex) {
            throw ex;
        } catch (Exception ex) {
            log.debug("*** ошибка добавления лида " + ex.getMessage(), ex);
            return CreateLeadResult.of(false);
        }
    }

    public CheckLeadResult getAllFromCheckLead(List<String> innList, Long fileId) {

        log.debug("*** Check duplicate in Bank");

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
            return CheckLeadResult.of(true, null, response.getBody().getLeads());
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

    @Override
    public CheckLeadResult getCheckLeadResult(String id, Long fileId) throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    @Override
    public boolean getCreateLeadResult(String id) throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }
}
