package ru.medvedev.importer.component;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import ru.medvedev.importer.service.bankclientservice.VtbAuthClientService;

import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RequiredArgsConstructor
public class VtbFeignInterceptor {

    private static final String TOKEN_TYPE = "Bearer ";
    private final VtbProperties parameter;
    private final VtbAuthClientService clientService;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (requestTemplate) -> {
            requestTemplate.removeHeader(AUTHORIZATION);
            requestTemplate.header(AUTHORIZATION, TOKEN_TYPE +
                    Optional.ofNullable(parameter.getAccessToken()).orElse(clientService.login()));
        };
    }
}
