package ru.medvedev.importer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.medvedev.importer.config.RetryerConfiguration;
import ru.medvedev.importer.dto.request.LoginRequest;
import ru.medvedev.importer.dto.response.LoginResponse;

import java.net.URI;

@FeignClient(value = "vtbAuth", url = "${vtb.token-url}", configuration = {
        RetryerConfiguration.class
})
public interface VtbAuthClient {

    @PostMapping(consumes = "application/x-www-form-urlencoded")
    LoginResponse login(URI baseUrl, @RequestBody LoginRequest request);
}
