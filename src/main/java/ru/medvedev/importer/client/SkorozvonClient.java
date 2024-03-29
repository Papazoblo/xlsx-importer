package ru.medvedev.importer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.medvedev.importer.component.SkorozvonErrorDecoder;
import ru.medvedev.importer.component.SkorozvonFeignInterceptor;
import ru.medvedev.importer.config.RetryerConfiguration;
import ru.medvedev.importer.dto.ScenarioDto;
import ru.medvedev.importer.dto.request.CreateMultipleRequest;
import ru.medvedev.importer.dto.request.ImportLeadRequest;
import ru.medvedev.importer.dto.response.ImportLeadResponse;
import ru.medvedev.importer.dto.response.LoginResponse;

@FeignClient(value = "skorozvon", name = "skorozvon", url = "${skorozvon.url}", configuration = {
        RetryerConfiguration.class, SkorozvonFeignInterceptor.class, SkorozvonErrorDecoder.class
})
public interface SkorozvonClient {

    @GetMapping("/api/v2/scenarios/{id}")
    ScenarioDto getScenarioById(@PathVariable("id") Long id);

    @PostMapping("/api/v2/leads/import")
    ImportLeadResponse importLeads(@RequestBody ImportLeadRequest request);

    @PostMapping("/api/v2/leads/create_multi")
    LoginResponse createMultipleLeads(@RequestBody CreateMultipleRequest request);

}
