package ru.medvedev.importer.component;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
public class OpeningFeignInterceptor {

    private static final String HEADER_NAME = "X-Auth-Token";
    private final OpeningProperties parameter;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (requestTemplate) -> {
            requestTemplate.removeHeader(HEADER_NAME);
            requestTemplate.header(HEADER_NAME, parameter.getApiKey());
        };
    }
}
