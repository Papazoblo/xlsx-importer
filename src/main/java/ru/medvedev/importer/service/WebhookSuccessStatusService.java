package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.WebhookSuccessStatusDto;
import ru.medvedev.importer.entity.WebhookSuccessStatusEntity;
import ru.medvedev.importer.repository.WebhookSuccessStatusRepository;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WebhookSuccessStatusService {

    private final WebhookSuccessStatusRepository repository;

    public Page<WebhookSuccessStatusDto> getPage(Pageable pageable) {
        Page<WebhookSuccessStatusEntity> page = repository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream()
                .map(WebhookSuccessStatusDto::of).collect(Collectors.toList()),
                page.getPageable(), page.getTotalElements());
    }

    public boolean existByName(String name) {
        return repository.existsByName(name.trim());
    }

    public void create(String name) {
        String trimName = name.trim();
        if (!repository.existsByName(trimName)) {
            WebhookSuccessStatusEntity entity = new WebhookSuccessStatusEntity();
            entity.setName(trimName);
            repository.save(entity);
        }
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
