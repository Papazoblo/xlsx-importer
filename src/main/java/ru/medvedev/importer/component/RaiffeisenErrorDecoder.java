package ru.medvedev.importer.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.medvedev.importer.dto.raiffeisen.RaiffeisenErrorDto;
import ru.medvedev.importer.dto.raiffeisen.RaiffeisenErrorResponseDto;
import ru.medvedev.importer.dto.response.CheckLeadBadRequestResponse;
import ru.medvedev.importer.exception.ErrorCreateLeadException;
import ru.medvedev.importer.exception.TimeOutException;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class RaiffeisenErrorDecoder implements ErrorDecoder {

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
                log.debug("*** raiffeisen badRequest");

                ObjectMapper objectMapper = new ObjectMapper();
                RaiffeisenErrorResponseDto responseBody = objectMapper.readValue(response.body().asInputStream(),
                        RaiffeisenErrorResponseDto.class);
                throw new ErrorCreateLeadException(String.format("%s\n%s", responseBody.getError().getErrorMessage(),
                        responseBody.getError().getDetails().stream()
                                .map(detail -> String.format("%s: %s", detail.getCause(), detail.getMessage()))
                                .collect(Collectors.joining("\n"))), -1L);
            } catch (IOException e) {
                log.debug("*** error parse response body", e);
            }
        }

        if (response.status() == 408) {
            throw new TimeOutException(s);
        }

        if (response.status() >= 500) {
            log.debug("*** raiffeisen error {}", response.body());
            throw new RetryableException(response.status(), response.toString(),
                    response.request().httpMethod(),
                    Date.from(LocalDateTime.now().plusMinutes(2).atZone(ZoneId.systemDefault()).toInstant()),
                    response.request());
        }

        return errorDecoder.decode(s, response);
    }
}
