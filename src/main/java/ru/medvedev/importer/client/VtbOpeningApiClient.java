package ru.medvedev.importer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.medvedev.importer.component.VtbOpeningErrorDecoder;
import ru.medvedev.importer.component.VtbOpeningFeignInterceptor;
import ru.medvedev.importer.config.RetryerConfiguration;
import ru.medvedev.importer.dto.request.VtbOpeningCheckInn;
import ru.medvedev.importer.dto.request.VtbOpeningCreateLead;
import ru.medvedev.importer.dto.response.VtbOpeningCheckInnResponse;
import ru.medvedev.importer.dto.response.VtbOpeningCheckResultResponse;
import ru.medvedev.importer.dto.response.VtbOpeningResponse;

@FeignClient(value = "vtbOpeningApi", url = "${vtb-opening.url}", configuration = {
        RetryerConfiguration.class, VtbOpeningErrorDecoder.class, VtbOpeningFeignInterceptor.class
})
public interface VtbOpeningApiClient {

    @PostMapping("${vtb-opening.url-check-inn}")
    ResponseEntity<VtbOpeningResponse> createCheckLeadsRequest(@RequestBody VtbOpeningCheckInn body);

    @GetMapping("${vtb-opening.url-check-inn}")
    ResponseEntity<VtbOpeningCheckInnResponse> getCheckLeadsResponse(@RequestParam("id") String id);

    @PostMapping("${vtb-opening.url-add-request}")
    ResponseEntity<VtbOpeningResponse> creteAddLeadRequest(@RequestBody VtbOpeningCreateLead request);

    @GetMapping("${vtb-opening.url-check-request-status}")
    ResponseEntity<VtbOpeningCheckResultResponse> getAddLeadResponse(@RequestParam("id") String id);
}
