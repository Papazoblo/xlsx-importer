package ru.medvedev.importer.service.bankclientservice;

import ru.medvedev.importer.dto.WebhookLeadDto;
import ru.medvedev.importer.dto.response.LeadInfoResponse;

import java.util.List;
import java.util.stream.Collectors;

import static ru.medvedev.importer.enums.CheckLeadStatus.POSITIVE;

public interface BankClientService {

    boolean createLead(WebhookLeadDto webhookLead);

    default List<LeadInfoResponse> getPositiveFromCheckLead(List<String> innList, Long fileId) {
        return getAllFromCheckLead(innList, fileId).stream().filter(item -> item.getResponseCode() == POSITIVE)
                .collect(Collectors.toList());
    }

    List<LeadInfoResponse> getAllFromCheckLead(List<String> innList, Long fileId);
}
