package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.RequestStatisticProjection;
import ru.medvedev.importer.entity.RequestEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.RequestStatus;
import ru.medvedev.importer.repository.RequestRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository repository;

    public RequestEntity save(RequestEntity entity) {
        return repository.save(entity);
    }

    public Optional<RequestEntity> getFirstByStatus(RequestStatus status) {
        return repository.findFirstByStatusOrderByLastCheckAsc(status);
    }

    public Optional<RequestEntity> getFirstByStatusAndBank(RequestStatus status, Bank bank) {
        List<RequestEntity> requests = repository.findFirstByStatusAndBank(status, bank);
        if (requests.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(requests.get(0));
    }

    public Map<Bank, Map<RequestStatus, Integer>> getStatisticByFileId(Long fileId) {
        Map<Bank, Map<RequestStatus, Integer>> result = new HashMap<>();
        repository.getStatisticByFile(fileId).stream()
                .collect(Collectors.groupingBy(RequestStatisticProjection::getBank))
                .entrySet()
                .forEach(entry -> result.put(entry.getKey(), entry.getValue().stream()
                        .collect(Collectors.toMap(RequestStatisticProjection::getStatus,
                                RequestStatisticProjection::getCount))));
        return result;
    }

    public void deleteByFibId(Set<Long> fibId) {
        repository.deleteAllByFileInfoBankIdIn(fibId);
    }

    public void changeStatus(Long id, RequestStatus newStatus) {
        repository.updateStatusById(id, newStatus);
    }
}
