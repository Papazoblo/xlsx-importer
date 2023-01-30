package ru.medvedev.importer.service.bankclientservice;

import ru.medvedev.importer.dto.CheckLeadResult;
import ru.medvedev.importer.dto.CreateLeadResult;
import ru.medvedev.importer.dto.WebhookLeadDto;

import javax.naming.OperationNotSupportedException;
import java.util.List;
import java.util.stream.Collectors;

import static ru.medvedev.importer.enums.CheckLeadStatus.POSITIVE;

public interface BankClientService {

    CreateLeadResult createLead(WebhookLeadDto webhookLead);

    default CheckLeadResult getPositiveFromCheckLead(List<String> innList, Long fileId) {
        CheckLeadResult result = getAllFromCheckLead(innList, fileId);
        result.setLeadResponse(result.getLeadResponse().stream().filter(item -> item.getResponseCode() == POSITIVE)
                .collect(Collectors.toList()));
        return result;
    }

    CheckLeadResult getAllFromCheckLead(List<String> innList, Long fileId);

    CheckLeadResult getCheckLeadResult(String id, Long fileId) throws OperationNotSupportedException;

    boolean getCreateLeadResult(String id) throws OperationNotSupportedException;
}
