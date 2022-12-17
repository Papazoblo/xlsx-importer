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
import ru.medvedev.importer.dto.events.NotificationEvent;
import ru.medvedev.importer.entity.EventEntity;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.ContactStatus;
import ru.medvedev.importer.enums.EventType;
import ru.medvedev.importer.enums.FileSource;
import ru.medvedev.importer.repository.EventRepository;
import ru.medvedev.importer.service.telegram.notification.TelegramNotificatorPollingService;
import ru.medvedev.importer.service.telegram.xlsxcollector.TelegramPollingService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    public static final String BANK_NAME_PATTERN = "Банк: `%s`\n";
    public static final String NOTIFICATION_PATTERN = BANK_NAME_PATTERN + "Статус: `%s`\n\nИНН: `%s`\nГород: `%s`\nИмя: `%s`";
    public static final String STATISTIC_LINE_PATTERN = "%s: `%d`\n";
    private static final String MESSAGE_SIMPLE_PATTERN = "%s";
    private static final String MESSAGE_HEADER_PATTERN = "*%s* %s\nФайл: `%s`\nИсточник: `%s`\n%s";
    private static final String MESSAGE_STATISTIC_PATTERN = "Статистика загрузки `%s`\nФайл: `%s`\nИсточник: `%s`\n" + STATISTIC_LINE_PATTERN + STATISTIC_LINE_PATTERN + STATISTIC_LINE_PATTERN;

    private final EventRepository repository;
    private final TelegramPollingService telegramPollingService;
    private final TelegramNotificatorPollingService telegramNotificatorPollingService;
    private final FileInfoService fileInfoService;
    private final ContactNewService contactService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationChatService notificationChatService;

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
            if (event.getFileId() == -1) {
                telegramPollingService.sendMessage(String.format(MESSAGE_SIMPLE_PATTERN, event.getDescription()),
                        null, null, null, event.isWithCancelButton());
            } else {
                FileInfoEntity fileInfo = fileInfoService.getById(fileId);
                if (event.getEventType() == EventType.FILE_PROCESS) {
                    printFileProcessInfo(fileInfo, event);
                } else if (event.getEventType() != EventType.LOG && fileInfo.getChatId() != null) {
                    telegramPollingService.sendMessage(initHeaderMessage(fileInfo, event, event.getDescription()),
                            null, fileInfo.getMessageId(), fileInfo.getChatId(), event.isWithCancelButton());
                    /*if (event.getEventType() == EventType.SUCCESS) {
                        printStatistic(chatId, file);
                    }*/
                }
            }
        });

        if (event.getEventType() != EventType.FILE_PROCESS) {
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
    }

    @EventListener(NotificationEvent.class)
    public void sendNotification(NotificationEvent event) {
        notificationChatService.getAllChatId().forEach(chatId ->
                telegramNotificatorPollingService.sendMessage(event.getDescription(), chatId));
    }

    private void printFileProcessInfo(FileInfoEntity file, ImportEvent event) {
        String message = initHeaderMessage(file, event, event.getDescription());
        if (file.getMessageId() != null) {
            telegramPollingService.updateMessage(message, file.getMessageId(), file.getChatId(), false);
        } else {
            telegramPollingService.sendMessage(message, file.getId(), null, file.getChatId(), false);
        }
    }

    private static String initHeaderMessage(FileInfoEntity fileInfo, ImportEvent event, String additional) {
        return String.format(MESSAGE_HEADER_PATTERN,
                event.getEventType().getDescription(),
                getCurDateTime(),
                fileInfo.getName(),
                fileInfo.getSource().getDescription(),
                additional);
    }

    private void printStatistic(Long chatId, FileInfoEntity fileInfo) {
        Map<ContactStatus, Long> mapStatistic = contactService.getContactStatisticByFileId(fileInfo.getId());
        if (fileInfo.getSource() == FileSource.UI) {
            return;
        }
        telegramPollingService.sendMessage(String.format(MESSAGE_STATISTIC_PATTERN,
                getCurDateTime(),
                fileInfo.getName(),
                fileInfo.getSource().getDescription(),
                ContactStatus.ADDED.getDescription(),
                Optional.ofNullable(mapStatistic.get(ContactStatus.ADDED)).orElse(0L),
                ContactStatus.DOWNLOADED.getDescription(),
                Optional.ofNullable(mapStatistic.get(ContactStatus.DOWNLOADED)).orElse(0L),
                ContactStatus.REJECTED.getDescription(),
                Optional.ofNullable(mapStatistic.get(ContactStatus.REJECTED)).orElse(0L)),
                null, null, chatId, false);
    }

    private static String getCurDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }
}
