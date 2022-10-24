package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.RequestStatisticProjection;
import ru.medvedev.importer.entity.OpeningRequestEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.OpeningRequestStatus;
import ru.medvedev.importer.repository.OpeningRequestRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpeningRequestService {

    private final OpeningRequestRepository repository;

    public void save(OpeningRequestEntity entity) {
        repository.save(entity);
    }

    public Optional<OpeningRequestEntity> getFirstByStatus(OpeningRequestStatus status) {
        return repository.findFirstByStatusOrderByLastCheckAsc(status);
    }

    public Optional<OpeningRequestEntity> getFirstByStatusAndBank(OpeningRequestStatus status, Bank bank) {
        List<OpeningRequestEntity> requests = repository.findFirstByStatusAndBank(status, bank);
        if (requests.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(requests.get(0));
    }

    public Map<Bank, Map<OpeningRequestStatus, Integer>> getStatisticByFileId(Long fileId) {
        Map<Bank, Map<OpeningRequestStatus, Integer>> result = new HashMap<>();
        repository.getStatisticByFile(fileId).stream()
                .collect(Collectors.groupingBy(RequestStatisticProjection::getBank))
                .entrySet()
                .forEach(entry -> result.put(entry.getKey(), entry.getValue().stream()
                        .collect(Collectors.toMap(RequestStatisticProjection::getStatus,
                                RequestStatisticProjection::getCount))));
        return result;
    }

    public void changeStatus(Long id, OpeningRequestStatus newStatus) {
        repository.updateStatusById(id, newStatus);
    }
}
