package ru.medvedev.importer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.medvedev.importer.component.RaiffeisenErrorDecoder;
import ru.medvedev.importer.component.RaiffeisenFeignInterceptor;
import ru.medvedev.importer.component.VtbErrorDecoder;
import ru.medvedev.importer.component.VtbFeignInterceptor;
import ru.medvedev.importer.config.RetryerConfiguration;
import ru.medvedev.importer.dto.raiffeisen.RaiffeisenRequestCreateLeadDto;
import ru.medvedev.importer.dto.request.LeadRequest;
import ru.medvedev.importer.dto.response.AddLeadResponse;
import ru.medvedev.importer.dto.response.CheckLeadResponse;

@FeignClient(value = "raiffeisenApi", name = "raiffeisenApi", url = "${raiffeisen.url}", configuration = {
        RetryerConfiguration.class, RaiffeisenErrorDecoder.class, RaiffeisenFeignInterceptor.class
})
public interface RaiffeisenRequestApiClient {

    @PostMapping
    ResponseEntity<Object> createRequest(@RequestBody RaiffeisenRequestCreateLeadDto requestBody);
}
