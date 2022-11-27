package ru.medvedev.importer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.medvedev.importer.config.RetryerConfiguration;
import ru.medvedev.importer.dto.request.LoginRequest;
import ru.medvedev.importer.dto.request.RefreshRequest;
import ru.medvedev.importer.dto.response.LoginResponse;

@FeignClient(value = "skorozvonAuth", name = "skorozvonAuth", url = "${skorozvon.url}", configuration = {
        RetryerConfiguration.class
})
public interface SkorozvonAuthClient {

    @PostMapping("/oauth/token")
    LoginResponse login(@RequestBody LoginRequest request);

    @PostMapping("/oauth/token")
    LoginResponse refresh(@RequestBody RefreshRequest request,
                          @RequestHeader(name = "Authorization") String token);
}
