package ru.medvedev.importer.component;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.medvedev.importer.exception.TimeOutException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class SkorozvonErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder errorDecoder = new Default();
    private final SkorozvonProperties parameter;

    @SneakyThrows
    @Override
    public Exception decode(String s, Response response) {

        if (response.status() == 401) {
            parameter.clearToken();
        }

        if(response.status() == 408) {
            throw new TimeOutException(s);
        }

        if (response.status() > 300) {
            throw new RetryableException(response.status(),
                    new BufferedReader(
                            new InputStreamReader(response.body().asInputStream(), StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n")),
                    response.request().httpMethod(), null, response.request());
        }

        return errorDecoder.decode(s, response);
    }
}
