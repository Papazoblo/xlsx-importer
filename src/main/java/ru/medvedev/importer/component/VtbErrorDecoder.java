package ru.medvedev.importer.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.medvedev.importer.dto.LeadDto;
import ru.medvedev.importer.dto.request.LeadRequest;
import ru.medvedev.importer.dto.response.CheckLeadBadRequestResponse;
import ru.medvedev.importer.dto.response.CheckLeadResponse;
import ru.medvedev.importer.enums.CheckLeadStatus;
import ru.medvedev.importer.exception.ErrorCreateVtbLeadException;
import ru.medvedev.importer.exception.TimeOutException;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class VtbErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder errorDecoder = new Default();
    private final VtbProperties properties;

    @Override
    public Exception decode(String s, Response response) {

        if (response.status() == 401) {
            properties.clearToken();
            throw new RetryableException(response.status(), response.toString(),
                    response.request().httpMethod(),
                    Date.from(LocalDateTime.now().plusMinutes(2).atZone(ZoneId.systemDefault()).toInstant()),
                    response.request());
        }

        if (response.status() == 400) {

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                if (response.request().url().contains("/check_leads")) {
                    CheckLeadResponse responseBody = objectMapper.readValue(response.body().asInputStream(), CheckLeadResponse.class);
                    List<LeadDto> leads = responseBody.getLeads()
                            .stream()
                            .filter(lead -> {
                                if (lead.getResponseCode() != CheckLeadStatus.INVALID_INN) {
                                    return true;
                                }
                                log.debug("*** INVALID_INN {}", lead.getInn());
                                return false;
                            })
                            .map(lead -> {
                                LeadDto newLead = new LeadDto();
                                newLead.setInn(lead.getInn());
                                return newLead;
                            })
                            .collect(Collectors.toList());
                    LeadRequest leadRequest = new LeadRequest();
                    leadRequest.setLeads(leads);

                    response.request().requestTemplate().body(objectMapper.writeValueAsString(leadRequest));
                    throw new RetryableException(response.status(), response.toString(),
                            response.request().httpMethod(),
                            null,
                            response.request());
                } else if (response.request().url().contains("/leads_impersonal")) {
                    CheckLeadBadRequestResponse responseBody = objectMapper.readValue(response.body().asInputStream(), CheckLeadBadRequestResponse.class);
                    responseBody.setMoreInformation(responseBody.getLeads().stream()
                            .map(item -> String.format("%s: %s", item.getInn(), item.getResponseCodeDescription()))
                            .collect(Collectors.joining("\n")));
                    throw new ErrorCreateVtbLeadException(responseBody.getMoreInformation(), -1L);
                }
            } catch (IOException e) {
                log.debug("*** error updating check lead request", e);
            }
        }

        if (response.status() == 408) {
            throw new TimeOutException(s);
        }

        if (response.status() >= 500) {
            log.debug("*** vtb error {}", response.body());
            throw new RetryableException(response.status(), response.toString(),
                    response.request().httpMethod(),
                    Date.from(LocalDateTime.now().plusMinutes(2).atZone(ZoneId.systemDefault()).toInstant()),
                    response.request());
        }

        return errorDecoder.decode(s, response);
    }
}
