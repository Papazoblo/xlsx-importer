package ru.medvedev.importer.component;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class VtbOpeningErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String s, Response response) {

        if (response.status() > 300) {
            log.debug("*** vtb-opening error {}", response.body());
            throw new RetryableException(response.status(), response.toString(),
                    response.request().httpMethod(), null, response.request());
        }

        return errorDecoder.decode(s, response);
    }
}
