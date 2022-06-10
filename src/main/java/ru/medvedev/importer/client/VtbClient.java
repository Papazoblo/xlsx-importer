package ru.medvedev.importer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.medvedev.importer.dto.request.LeadRequest;
import ru.medvedev.importer.dto.request.LoginRequest;
import ru.medvedev.importer.dto.response.AddLeadResponse;
import ru.medvedev.importer.dto.response.CheckLeadResponse;
import ru.medvedev.importer.dto.response.LoginResponse;

import java.net.URI;

@FeignClient(value = "vtb", url = "${vtb.url}")
public interface VtbClient {

    @PostMapping
    LoginResponse login(URI baseUrl, @RequestBody LoginRequest request);

    @PostMapping("/check_leads")
    CheckLeadResponse checkLeads(@RequestBody LeadRequest request);

    @PostMapping("/leads_impersonal")
    AddLeadResponse addLead(@RequestBody LeadRequest request);
}
