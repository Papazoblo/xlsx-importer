package ru.medvedev.importer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.medvedev.importer.component.OpeningErrorDecoder;
import ru.medvedev.importer.component.OpeningFeignInterceptor;
import ru.medvedev.importer.config.RetryerConfiguration;
import ru.medvedev.importer.dto.request.OpeningCheckInn;
import ru.medvedev.importer.dto.request.OpeningCreateLead;
import ru.medvedev.importer.dto.response.OpeningCheckInnResponse;
import ru.medvedev.importer.dto.response.OpeningCheckResultResponse;
import ru.medvedev.importer.dto.response.OpeningResponse;

@FeignClient(value = "vtbOpeningApi", name = "vtbOpeningApi", url = "${vtb-opening.url}", configuration = {
        RetryerConfiguration.class, OpeningErrorDecoder.class, OpeningFeignInterceptor.class
})
public interface OpeningApiClient {

    @PostMapping("${vtb-opening.url-check-inn}")
    ResponseEntity<OpeningResponse> createCheckLeadsRequest(@RequestBody OpeningCheckInn body);

    @GetMapping("${vtb-opening.url-check-inn}")
    ResponseEntity<OpeningCheckInnResponse> getCheckLeadsResponse(@RequestParam("id") String id);

    @PostMapping(value = "${vtb-opening.url-add-request}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<OpeningResponse> creteAddLeadRequest(@RequestBody OpeningCreateLead request);

    @GetMapping("${vtb-opening.url-check-request-status}")
    ResponseEntity<OpeningCheckResultResponse> getAddLeadResponse(@RequestParam("id") String id);
}
