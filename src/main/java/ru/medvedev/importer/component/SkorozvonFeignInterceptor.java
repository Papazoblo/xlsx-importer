package ru.medvedev.importer.component;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import ru.medvedev.importer.service.SkorozvonAuthClientService;

import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RequiredArgsConstructor
public class SkorozvonFeignInterceptor {

    private static final String TOKEN_TYPE = "Bearer ";
    private final SkorozvonProperties parameter;
    private final SkorozvonAuthClientService clientService;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (requestTemplate) -> {
            requestTemplate.removeHeader(AUTHORIZATION);
            requestTemplate.header(AUTHORIZATION, TOKEN_TYPE +
                    Optional.ofNullable(parameter.getAccessToken()).orElse(clientService.apiLogin()));
        };
    }
}
