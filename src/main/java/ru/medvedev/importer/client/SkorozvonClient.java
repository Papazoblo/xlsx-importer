package ru.medvedev.importer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.medvedev.importer.component.SkorozvonErrorDecoder;
import ru.medvedev.importer.component.SkorozvonFeignInterceptor;
import ru.medvedev.importer.config.RetryerConfiguration;
import ru.medvedev.importer.dto.request.CreateMultipleRequest;
import ru.medvedev.importer.dto.request.ImportLeadRequest;
import ru.medvedev.importer.dto.response.ImportLeadResponse;
import ru.medvedev.importer.dto.response.LoginResponse;

@FeignClient(value = "skorozvon", url = "${skorozvon.url}", configuration = {
        RetryerConfiguration.class, SkorozvonFeignInterceptor.class, SkorozvonErrorDecoder.class
})
public interface SkorozvonClient {

    @PostMapping("/api/v2/leads/import")
    ImportLeadResponse importLeads(@RequestBody ImportLeadRequest request);

    @PostMapping("/api/v2/leads/create_multi")
    LoginResponse createMultipleLeads(@RequestBody CreateMultipleRequest request);

}
