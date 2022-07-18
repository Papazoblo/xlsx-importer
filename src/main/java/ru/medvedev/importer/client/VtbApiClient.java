package ru.medvedev.importer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.medvedev.importer.component.VtbErrorDecoder;
import ru.medvedev.importer.component.VtbFeignInterceptor;
import ru.medvedev.importer.config.RetryerConfiguration;
import ru.medvedev.importer.dto.request.LeadRequest;
import ru.medvedev.importer.dto.response.AddLeadResponse;
import ru.medvedev.importer.dto.response.CheckLeadResponse;

@FeignClient(value = "vtbApi", url = "${vtb.url}", configuration = {
        RetryerConfiguration.class, VtbErrorDecoder.class, VtbFeignInterceptor.class
})
public interface VtbApiClient {

    @PostMapping("/lead-impers/v1/check_leads")
    ResponseEntity<CheckLeadResponse> checkLeads(@RequestBody LeadRequest request,
                                                 @RequestHeader(name = "Authorization") String token);

    @PostMapping("/lead-impers/v1/leads_impersonal")
    AddLeadResponse addLead(@RequestBody LeadRequest request,
                            @RequestHeader(name = "Authorization") String token);
}
