package ru.medvedev.importer.component;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
@Slf4j
public class AlfaFeignInterceptor {

    private static final String HEADER_NAME = "API-key";
    private final AlfaProperties parameter;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (requestTemplate) -> {
            requestTemplate.removeHeader(HEADER_NAME);
            requestTemplate.header(HEADER_NAME, parameter.getApiKey());
            log.debug(requestTemplate.method() + " " + requestTemplate.path());
            if (requestTemplate.body() != null) {
                log.debug(new String(requestTemplate.body()));
            }
        };
    }
}
