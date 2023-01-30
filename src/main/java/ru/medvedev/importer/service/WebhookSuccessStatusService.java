package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.WebhookStatusDto;
import ru.medvedev.importer.dto.WebhookSuccessFilter;
import ru.medvedev.importer.dto.WebhookSuccessStatusDto;
import ru.medvedev.importer.entity.WebhookSuccessStatusEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.WebhookType;
import ru.medvedev.importer.repository.WebhookSuccessStatusRepository;
import ru.medvedev.importer.specification.WebhookSuccessSpecification;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookSuccessStatusService {

    private final WebhookSuccessStatusRepository repository;

    public List<WebhookStatusDto> findWebhookStatusByType(List<WebhookType> type) {
        List<WebhookStatusDto> statusList = new ArrayList<>();
        statusList.add(new WebhookStatusDto(-2L, "Отказ_Недозвон"));
        statusList.addAll(repository.findAllByTypeInOrderByWebhookId(type).stream()
                .map(WebhookSuccessStatusEntity::getWebhookStatusEntity)
                .map(WebhookStatusDto::of)
                .collect(Collectors.toSet()));
        return statusList;
    }

    public Page<WebhookSuccessStatusDto> getPage(Pageable pageable, WebhookSuccessFilter filter) {
        Page<WebhookSuccessStatusEntity> page = repository.findAll(WebhookSuccessSpecification.of(filter), pageable);
        return new PageImpl<>(page.getContent().stream()
                .map(WebhookSuccessStatusDto::of).collect(toList()),
                page.getPageable(), page.getTotalElements());
    }

    public List<WebhookSuccessStatusEntity> getByWebhook(Long webhookId) {
        return repository.findAllByBankAndWebhookId(webhookId);
    }

    public List<WebhookSuccessStatusEntity> getByBankAndWebhook(Bank bank, Long webhookId) {
        return repository.findAllByBankAndWebhookId(bank, webhookId);
    }

    public boolean existByName(WebhookSuccessFilter filter) {
        return repository.count(WebhookSuccessSpecification.of(filter)) > 0;
    }

    public Optional<WebhookSuccessStatusEntity> getByNameAndStatus(String name) {
        return repository.findByNameAndType(name);
    }

    public WebhookSuccessStatusEntity getByName(WebhookSuccessFilter filter) {
        List<WebhookSuccessStatusEntity> webhooks = repository.findAll(WebhookSuccessSpecification.of(filter));
        if (webhooks.isEmpty()) {
            return null;
        }
        return webhooks.get(0);
    }

    public void create(WebhookSuccessStatusDto input) throws CloneNotSupportedException {
        //String trimName = input.getName().trim();
        WebhookSuccessStatusEntity entity = new WebhookSuccessStatusEntity();
        //entity.setWebhookId(webhookStatusService.creatIfNotExists(trimName).getId());
        entity.setWebhookId(input.getStatusId());
        entity.setBank(input.getBank());
        entity.setType(input.getType());

        /*if (input.getType() == WebhookType.ERROR) {
            WebhookSuccessStatusEntity cloneEntity = entity.clone();
            entity.setBank(input.getBank());
            cloneEntity.setBank(Bank.VTB_OPENING);
            repository.save(cloneEntity);
        }*/
        try {
            if (!repository.existsByWebhookIdAndBankAndType(input.getStatusId(), input.getBank(), input.getType())) {
                repository.save(entity);
            }
        } catch (ConstraintViolationException ex) {
            log.debug("Такая связка Вебхука, типа и банка уже добавлена: {}", input);
        }
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
