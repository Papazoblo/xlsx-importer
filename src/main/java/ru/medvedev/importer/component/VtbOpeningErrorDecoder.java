package ru.medvedev.importer.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.medvedev.importer.dto.response.VtbOpeningErrorResponse;
import ru.medvedev.importer.exception.ErrorCreateVtbLeadException;

@Slf4j
@RequiredArgsConstructor
public class VtbOpeningErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder errorDecoder = new Default();

    private final ObjectMapper objectMapper;

    @Override
    public Exception decode(String s, Response response) {

        if (response.status() == 400) {
            log.debug("*** vtb-opening error {}", response.body());
            if (response.request().url().contains("/status") || response.request().url().contains("/add")) {
                VtbOpeningErrorResponse responseDto;
                try {
                    responseDto = objectMapper.readValue(response.body().asInputStream(), VtbOpeningErrorResponse.class);
                } catch (Exception ex) {
                    throw new ErrorCreateVtbLeadException("Невозможно распарсить ответ", -1L);
                }
                throw new ErrorCreateVtbLeadException(responseDto.getDescription(), -1L);

            }
        }

        if (response.status() > 300) {
            log.debug("*** vtb-opening error {}", response.body());
            throw new RetryableException(response.status(), response.toString(),
                    response.request().httpMethod(), null, response.request());
        }

        return errorDecoder.decode(s, response);
    }
}
