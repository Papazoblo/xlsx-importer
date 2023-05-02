package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.entity.BankCountSendedRequestEntity;
import ru.medvedev.importer.entity.id.BankCountSendedRequestId;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.repository.BankCountSendedRequestRepository;

@Service
@RequiredArgsConstructor
public class BankCountSendedRequestService {

    public static int DAY_RESERVE = 500;

    private final BankCountSendedRequestRepository repository;

    public void incCreateRequestCount(Bank bank, int value) {
        BankCountSendedRequestEntity entity = getById(bank);
        entity.setCreateRequestCount(entity.getCreateRequestCount() + value);
        repository.save(entity);
    }

    public void incResultCreateRequestCount(Bank bank, int value) {
        BankCountSendedRequestEntity entity = getById(bank);
        entity.setResultCreateRequestCount(entity.getResultCreateRequestCount() + value);
        repository.save(entity);
    }

    public void incCheckRequestCount(Bank bank, int value) {
        BankCountSendedRequestEntity entity = getById(bank);
        entity.setCheckRequestCount(entity.getCheckRequestCount() + value);
        repository.save(entity);
    }

    public void incResultCheckRequestCount(Bank bank, int value) {
        BankCountSendedRequestEntity entity = getById(bank);
        entity.setResultCheckRequestCount(entity.getResultCheckRequestCount() + value);
        repository.save(entity);
    }

    public BankCountSendedRequestEntity getById(Bank bank) {
        return repository.findById(BankCountSendedRequestId.currentId(bank))
                .orElseGet(() -> {
                    BankCountSendedRequestEntity newEntity = new BankCountSendedRequestEntity();
                    newEntity.setId(BankCountSendedRequestId.currentId(bank));
                    return newEntity;
                });
    }
}
