package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.EventDto;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.entity.EventEntity;
import ru.medvedev.importer.repository.EventRepository;
import ru.medvedev.importer.service.telegram.TelegramPollingService;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository repository;
    private final TelegramPollingService telegramPollingService;
    private final FileInfoService fileInfoService;

    public Page<EventDto> getPage(Pageable pageable) {
        Page<EventEntity> page = repository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream()
                .map(EventDto::of).collect(Collectors.toList()),
                page.getPageable(), page.getTotalElements());
    }

    @EventListener(ImportEvent.class)
    public void create(ImportEvent event) {
        Long chatId = fileInfoService.getChatIdByFile(event.getFileId());
        if (chatId == null) {
            telegramPollingService.sendMessage(event.getDescription(), chatId);
        }

        EventEntity entity = new EventEntity();
        entity.setDescription(entity.getDescription());
        entity.setType(event.getEventType());
        entity.setFileId(event.getFileId());
        repository.save(entity);
    }
}
