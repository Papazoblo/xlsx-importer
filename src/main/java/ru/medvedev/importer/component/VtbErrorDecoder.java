package ru.medvedev.importer.component;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        if (response.status() > 300) {
            log.debug("*** vtb error {}", response.status());
            throw new RetryableException(response.status(), response.toString(),
                    response.request().httpMethod(), null, response.request());
        }

        return errorDecoder.decode(s, response);
    }
}
