package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.EventDto;
import ru.medvedev.importer.dto.events.CompleteFileEvent;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.dto.events.InvalidFileEvent;
import ru.medvedev.importer.entity.EventEntity;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.ContactStatus;
import ru.medvedev.importer.enums.EventType;
import ru.medvedev.importer.repository.EventRepository;
import ru.medvedev.importer.service.telegram.TelegramPollingService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    protected static final String MESSAGE_SIMPLE_PATTERN = "*Импорт с интерфейса* \n_%s_";
    protected static final String MESSAGE_PATTERN = "*%s* %s\n*Файл: %s*\n*Источник: %s*\n%s";
    protected static final String MESSAGE_STATISTIC_PATTERN = "*Статистика загрузки* %s\n*Файл: %s*\n*Источник: %s*\n%s: %d\n%s: %d\n%s: %d";

    private final EventRepository repository;
    private final TelegramPollingService telegramPollingService;
    private final FileInfoService fileInfoService;
    private final ContactService contactService;
    private final ApplicationEventPublisher eventPublisher;

    public Page<EventDto> getPage(Pageable pageable) {
        Page<EventEntity> page = repository.findAll(pageable);
        Set<Long> fileIds = page.getContent().stream().map(EventEntity::getFileId)
                .collect(Collectors.toSet());
        Map<Long, String> fileMap = fileInfoService.getFileNameMapByIds(fileIds);
        return new PageImpl<>(page.getContent().stream()
                .map(item -> EventDto.of(item, fileMap)).collect(Collectors.toList()),
                page.getPageable(), page.getTotalElements());
    }

    @EventListener(ImportEvent.class)
    public void create(ImportEvent event) {
        Optional.ofNullable(event.getFileId()).ifPresent(fileId -> {
            Long chatId = fileInfoService.getChatIdByFile(fileId);
            if (event.getEventType() != EventType.LOG && chatId != null) {
                Optional<FileInfoEntity> fileInfoEntity = Optional.ofNullable(event.getFileId())
                        .map(fileInfoService::getById);
                fileInfoEntity.ifPresent(file -> {
                    telegramPollingService.sendMessage(String.format(MESSAGE_PATTERN, event.getEventType().getDescription(),
                            getCurDateTime(),
                            file.getName(), file.getSource().getDescription(), event.getDescription()), chatId, event.isWithCancelButton());
                    if (event.getEventType() == EventType.SUCCESS) {
                        printStatistic(chatId, file);
                    }
                });
            } else if (event.getFileId() == -1) {
                telegramPollingService.sendMessage(String.format(MESSAGE_SIMPLE_PATTERN, event.getDescription()),
                        null, event.isWithCancelButton());
            }
        });

        EventEntity entity = new EventEntity();
        entity.setDescription(event.getDescription());
        entity.setType(event.getEventType());
        entity.setFileId(event.getFileId());
        repository.save(entity);

        if (event.getEventType() == EventType.ERROR) {
            eventPublisher.publishEvent(new InvalidFileEvent(this, event.getFileId()));
        } else if (event.getEventType() == EventType.SUCCESS) {
            eventPublisher.publishEvent(new CompleteFileEvent(this, event.getFileId()));
        }
    }

    private void printStatistic(Long chatId, FileInfoEntity fileInfo) {
        Map<ContactStatus, Long> mapStatistic = contactService.getContactStatisticByFileId(fileInfo.getId());
        telegramPollingService.sendMessage(String.format(MESSAGE_STATISTIC_PATTERN,
                getCurDateTime(),
                fileInfo.getName(),
                fileInfo.getSource().getDescription(),
                ContactStatus.ADDED.getDescription(),
                Optional.ofNullable(mapStatistic.get(ContactStatus.ADDED)).orElse(0L),
                ContactStatus.DOWNLOADED.getDescription(),
                Optional.ofNullable(mapStatistic.get(ContactStatus.DOWNLOADED)).orElse(0L),
                ContactStatus.REJECTED.getDescription(),
                Optional.ofNullable(mapStatistic.get(ContactStatus.REJECTED)).orElse(0L)), chatId, false);
    }

    private static String getCurDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }
}
