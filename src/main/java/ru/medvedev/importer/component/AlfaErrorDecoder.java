package ru.medvedev.importer.component;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import ru.medvedev.importer.dto.alfa.CheckErrorDto;
import ru.medvedev.importer.dto.alfa.CheckResponseDto;
import ru.medvedev.importer.dto.response.OpeningErrorResponse;
import ru.medvedev.importer.exception.ErrorCheckLeadException;
import ru.medvedev.importer.exception.ErrorCreateLeadException;
import ru.medvedev.importer.exception.TimeOutException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class AlfaErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder errorDecoder = new Default();

    private final ObjectMapper objectMapper;

    @Override
    public Exception decode(String s, Response response) {

        if (response.status() == 400 || response.status() == 403) {
            if ((response.request().url().contains("/checks") || response.request().url().contains("/leads"))
                    && response.request().httpMethod() == Request.HttpMethod.POST) {
                try {
                    if (response.request().url().contains("/checks")) {
                        CheckResponseDto responseDto = objectMapper.readValue(response.body().asInputStream(), CheckResponseDto.class);
                        throw new ErrorCheckLeadException(responseDto.getErrors().isEmpty() ? "" : responseDto.getErrors().stream()
                                .map(CheckErrorDto::getDetail)
                                .collect(Collectors.joining("\n")), -1L);
                    } else {
                        CheckResponseDto responseDto = objectMapper.readValue(response.body().asInputStream(), CheckResponseDto.class);
                        throw new ErrorCreateLeadException(responseDto.getErrors().isEmpty() ? "" : responseDto.getErrors().stream()
                                .map(CheckErrorDto::getDetail)
                                .collect(Collectors.joining("\n")), -1L);
                    }
                } catch (IOException e) {
                    throw new ErrorCreateLeadException("Невозможно распарсить ответ", -1L);
                }
            } else {
                log.debug("*** alfa bad request {}", response.body());
            }
        }

        if (response.status() == 408) {
            throw new TimeOutException(s);
        }

        if (response.status() >= 500) {
            String text;
            try {
                text = IOUtils.toString(response.body().asInputStream(), StandardCharsets.UTF_8);
                log.debug("*** alfa error {}", text);
            } catch (IOException e) {
                text = "undefined message";
                log.debug("*** alfa error [{}]", text);
            }
            throw new RetryableException(response.status(), text,
                    response.request().httpMethod(),
                    Date.from(LocalDateTime.now().plusMinutes(2).atZone(ZoneId.systemDefault()).toInstant()),
                    response.request());
        }

        return errorDecoder.decode(s, response);
    }
}
