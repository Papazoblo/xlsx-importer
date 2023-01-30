package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.WebhookStatusDto;
import ru.medvedev.importer.entity.WebhookStatusEntity;
import ru.medvedev.importer.exception.BadRequestException;
import ru.medvedev.importer.repository.WebhookStatusRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.isBlank;

@Service
@RequiredArgsConstructor
public class WebhookStatusService {

    private final WebhookStatusRepository repository;

    public List<WebhookStatusDto> getAll() {
        return repository.findAll().stream()
                .map(WebhookStatusDto::of)
                .sorted(Comparator.comparing(WebhookStatusDto::getName))
                .collect(Collectors.toList());
    }

    public WebhookStatusEntity getById(Long id) {
        return repository.getOne(id);
    }

    public WebhookStatusEntity creatIfNotExists(String name) {
        if (isBlank(name)) {
            throw new BadRequestException("Пустой результата вебхука");
        }
        return repository.findByName(name).orElseGet(() -> {
            WebhookStatusEntity entity = new WebhookStatusEntity();
            entity.setName(name);
            return repository.save(entity);
        });
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
