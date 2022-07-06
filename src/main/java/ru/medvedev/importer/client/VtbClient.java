package ru.medvedev.importer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.medvedev.importer.config.RetryerConfiguration;
import ru.medvedev.importer.dto.request.LeadRequest;
import ru.medvedev.importer.dto.request.LoginRequest;
import ru.medvedev.importer.dto.response.AddLeadResponse;
import ru.medvedev.importer.dto.response.CheckLeadResponse;
import ru.medvedev.importer.dto.response.LoginResponse;

import java.net.URI;

@FeignClient(value = "vtb", url = "${vtb.url}", configuration = {
        RetryerConfiguration.class
})
public interface VtbClient {

    @PostMapping(consumes = "application/x-www-form-urlencoded")
    LoginResponse login(URI baseUrl, @RequestBody LoginRequest request);

    @PostMapping("/lead-impers/v1/check_leads")
    ResponseEntity<CheckLeadResponse> checkLeads(@RequestBody LeadRequest request,
                                                 @RequestHeader(name = "Authorization") String token);

    @PostMapping("/lead-impers/v1/leads_impersonal")
    AddLeadResponse addLead(@RequestBody LeadRequest request,
                            @RequestHeader(name = "Authorization") String token);
}
