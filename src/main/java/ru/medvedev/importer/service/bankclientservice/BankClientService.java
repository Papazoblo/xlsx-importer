package ru.medvedev.importer.service.bankclientservice;

import ru.medvedev.importer.dto.CheckLeadResult;
import ru.medvedev.importer.dto.CreateLeadResult;
import ru.medvedev.importer.dto.WebhookLeadDto;
import ru.medvedev.importer.entity.WebhookStatisticEntity;

import javax.naming.OperationNotSupportedException;
import java.util.List;
import java.util.stream.Collectors;

import static ru.medvedev.importer.enums.CheckLeadStatus.POSITIVE;

public interface BankClientService {

    default CreateLeadResult createLead(WebhookLeadDto webhookLead) throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    default CheckLeadResult getAllFromCheckLead(WebhookStatisticEntity webhookStatisticEntity, List<String> innList, Long fileId) throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    default CheckLeadResult getCheckLeadResult(String id, Long fileId) throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    default boolean getCreateLeadResult(WebhookStatisticEntity webhookStatisticEntity) throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    default void getCityDictionary() throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }
}
