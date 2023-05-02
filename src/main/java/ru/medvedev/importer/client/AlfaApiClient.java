package ru.medvedev.importer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.medvedev.importer.component.AlfaErrorDecoder;
import ru.medvedev.importer.component.AlfaFeignInterceptor;
import ru.medvedev.importer.component.OpeningErrorDecoder;
import ru.medvedev.importer.component.OpeningFeignInterceptor;
import ru.medvedev.importer.config.RetryerConfiguration;
import ru.medvedev.importer.dto.alfa.*;
import ru.medvedev.importer.dto.request.OpeningCheckInn;
import ru.medvedev.importer.dto.request.OpeningCreateLead;
import ru.medvedev.importer.dto.response.OpeningCheckInnResponse;
import ru.medvedev.importer.dto.response.OpeningCheckResultResponse;
import ru.medvedev.importer.dto.response.OpeningResponse;

@FeignClient(value = "alfaApi", name = "alfaApi", url = "${alfa.url}", configuration = {
        RetryerConfiguration.class, AlfaErrorDecoder.class, AlfaFeignInterceptor.class
})
public interface AlfaApiClient {

    @PostMapping("${alfa.url-check-inn}")
    ResponseEntity<CheckResponseDto> createCheckLeadsRequest(@RequestBody CheckLeadInnDto body);

    @PostMapping("${alfa.url-add-request}")
    ResponseEntity<OpeningResponse> creteAddLeadRequest(@RequestBody CreateLeadRequestDto request);

    @GetMapping(value = "${alfa.url-request-status}")
    ResponseEntity<RequestStatusResponseDto> getRequestStatus(@PathVariable("id") String id);

    @GetMapping(value = "${alfa.dict-url}")
    ResponseEntity<CityResponseDto> getCityList();
}
