package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.client.SkorozvonClient;
import ru.medvedev.importer.dto.CreateLeadDto;
import ru.medvedev.importer.dto.CreateOrganizationDto;
import ru.medvedev.importer.dto.request.CreateMultipleRequest;
import ru.medvedev.importer.dto.request.ImportLeadRequest;
import ru.medvedev.importer.dto.response.ImportLeadResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkorozvonClientService {

    private final SkorozvonClient client;

    @Deprecated
    public void importLeads(Long projectId, List<CreateLeadDto> leads, List<String> tags) {
        ImportLeadRequest request = new ImportLeadRequest();
        request.setCallProjectId(projectId);
        request.setData(leads);
        request.setTags(tags);
        ImportLeadResponse response = client.importLeads(request);
        log.info("Import lead response {}", response);
    }

    public void createMultiple(Long projectId, List<CreateOrganizationDto> leads, List<String> tags) {
        CreateMultipleRequest request = new CreateMultipleRequest();
        request.setCallProjectId(projectId);
        request.setData(leads);
        request.setTags(tags);
        client.createMultipleLeads(request);
    }
}
