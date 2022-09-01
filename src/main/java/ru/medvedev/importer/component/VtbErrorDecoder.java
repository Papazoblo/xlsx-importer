package ru.medvedev.importer.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.medvedev.importer.dto.LeadDto;
import ru.medvedev.importer.dto.request.LeadRequest;
import ru.medvedev.importer.dto.response.CheckLeadBadRequestResponse;
import ru.medvedev.importer.enums.CheckLeadStatus;

import java.io.IOException;
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
        }

        if (response.status() == 400) {
            if (response.request().url().contains("/check_leads")) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    CheckLeadBadRequestResponse responseBody = objectMapper.readValue(response.body().asInputStream(), CheckLeadBadRequestResponse.class);
                    responseBody.setMoreInformation(String.format("[%s]", responseBody.getMoreInformation().replace("<BackErr> ", "")));
                    List<LeadDto> leads = objectMapper.readValue(responseBody.getMoreInformation(), new TypeReference<List<LeadDto>>() {
                    })
                            .stream()
                            .filter(lead -> {
                                if (lead.getResponseCode() != CheckLeadStatus.INVALID_INN) {
                                    return true;
                                }
                                log.debug("*** INVALID_INN {}", lead.getInn());
                                return false;
                            })
                            .peek(lead -> lead.setResponseCode(null))
                            .collect(Collectors.toList());
                    LeadRequest leadRequest = new LeadRequest();
                    leadRequest.setLeads(leads);

                    response.request().requestTemplate().body(objectMapper.writeValueAsString(leadRequest));
                } catch (IOException e) {
                    log.debug("*** error updating check lead request", e);
                }
            }
        }

        if (response.status() > 300) {
            log.debug("*** vtb error {}", response.body());
            throw new RetryableException(response.status(), response.toString(),
                    response.request().httpMethod(), null, response.request());
        }

        return errorDecoder.decode(s, response);
    }
}
