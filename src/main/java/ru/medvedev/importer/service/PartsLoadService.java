package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.PartsLoadDto;
import ru.medvedev.importer.dto.events.NotificationEvent;
import ru.medvedev.importer.entity.ContactNewEntity;
import ru.medvedev.importer.entity.PartsLoadEntity;
import ru.medvedev.importer.entity.WebhookStatisticEntity;
import ru.medvedev.importer.enums.*;
import ru.medvedev.importer.repository.ContactNewRepository;
import ru.medvedev.importer.repository.PartsLoadRepository;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.medvedev.importer.service.EventService.*;
import static ru.medvedev.importer.utils.StringUtils.getFioStringFromContact;

@RequiredArgsConstructor
@Service
public class PartsLoadService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final PartsLoadRepository repository;
    private final ContactNewRepository contactRepository;
    private final WebhookSuccessStatusService webhookSuccessStatusService;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "${cron.close-parts-load}")
    public void closePartsLoad() {
        repository.findAllByStatusNotFinished().forEach(item -> {
            if (item.getWebhookStatisticEntityList().stream()
                    .allMatch(webhookStatisticEntity -> Stream.of(WebhookStatus.ERROR, WebhookStatus.REJECTED, WebhookStatus.SUCCESS)
                            .anyMatch(status -> webhookStatisticEntity.getStatus() == status))) {
                item.setStatus(PartsLoadStatus.FINISHED);
                repository.save(item);

                Map<WebhookStatus, List<WebhookStatisticEntity>> webhookStatusListMap = item.getWebhookStatisticEntityList().stream()
                        .collect(Collectors.groupingBy(WebhookStatisticEntity::getStatus));
                eventPublisher.publishEvent(new NotificationEvent(this,
                        String.format(MESSAGE_PARTS_STATISTIC_PATTERN, getCurDateTime(),
                                "Дата с", formatter.format(item.getDateFrom()),
                                "Дата по", formatter.format(item.getDateTo()),
                                "Банк", item.getBank().getTitle(),
                                "Количетсво", item.getCount(),
                                "Фактическое количество", item.getWebhookStatisticEntityList().size(),
                                "Успешные", Optional.ofNullable(webhookStatusListMap.get(WebhookStatus.SUCCESS)).map(Collection::size).orElse(0),
                                "Отклоненные", Optional.ofNullable(webhookStatusListMap.get(WebhookStatus.REJECTED)).map(Collection::size).orElse(0),
                                "Ошибки", Optional.ofNullable(webhookStatusListMap.get(WebhookStatus.ERROR)).map(Collection::size).orElse(0)),
                        TelegramNotificationType.ALL,
                        EventType.LOG_TG));
            }
        });
    }

    public Page<PartsLoadDto> getPage(Pageable pageable) {
        Page<PartsLoadEntity> page = repository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream()
                .map(PartsLoadDto::of)
                .collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    public void create(PartsLoadDto input) {
        PartsLoadEntity entity = new PartsLoadEntity();
        entity.setCount(input.getCount());
        entity.setDateTo(input.getDateTo());
        entity.setDateFrom(input.getDateFrom());
        entity.setBank(input.getBank());
        entity.setWebhookStatisticEntityList(creatBatchToLoad(input));
        repository.save(entity);
    }

    private List<WebhookStatisticEntity> creatBatchToLoad(PartsLoadDto input) {

        LocalDateTime from = input.getDateFrom().minusMinutes(ZonedDateTime.now().getOffset().getTotalSeconds() / 60).plusMinutes(input.getTimeZone());
        LocalDateTime to = input.getDateTo().minusMinutes(ZonedDateTime.now().getOffset().getTotalSeconds() / 60).plusMinutes(input.getTimeZone());

        List<ContactNewEntity> contacts = contactRepository.findAllByAlfaCreateAtGreaterThanEqualAndAlfaCreateAtLessThanEqualOrderByCreateAt(from, to);
        if (contacts.size() > input.getCount()) {
            contacts = contacts.subList(0, input.getCount());
        }

        return contacts.stream()
                .map(contact -> {
                    WebhookStatisticEntity entity = new WebhookStatisticEntity();
                    entity.setStatus(WebhookStatus.FIXED);
                    entity.setCity(contact.getCity());
                    entity.setInn(contact.getInn());
                    entity.setBank(input.getBank());
                    entity.setPhone(contact.getPhone());
                    entity.setName(getFioStringFromContact(contact));
                    entity.setSkorozvonCreateAt(LocalDateTime.now());
                    entity.setFullName(entity.getName());
                    entity.setSuccessStatus(webhookSuccessStatusService.getByBank(input.getBank()));
                    return entity;
                })
                .collect(Collectors.toList());
    }
}
