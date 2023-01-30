package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.WebhookStatusMapDto;
import ru.medvedev.importer.entity.WebhookStatusMapEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.repository.WebhookStatusMapRepository;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class WebhookStatusMapService {

    private final WebhookStatusMapRepository repository;

    public List<WebhookStatusMapEntity> getList() {
        return repository.findAllWithSort();
    }

    public Map<Long, List<WebhookStatusMapEntity>> getMap(Bank bank) {
        return repository.findAllByBank(bank).stream()
                .collect(groupingBy(WebhookStatusMapEntity::getFromStatusId));
    }

    public void create(WebhookStatusMapDto input) {
        WebhookStatusMapEntity entity = new WebhookStatusMapEntity();
        entity.setActuality(input.getActuality());
        entity.setBank(input.getBank());
        entity.setErrorCount(input.getErrorCount());
        entity.setPriority(input.getPriority());
        entity.setFromStatusId(input.getWebhookStatus().getId());
        repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
