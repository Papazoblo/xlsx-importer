package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.entity.NotificationChatEntity;
import ru.medvedev.importer.enums.TelegramNotificationType;
import ru.medvedev.importer.repository.NotificationChatRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class NotificationChatService {

    private final NotificationChatRepository repository;

    public void addChatId(Long chatId, TelegramNotificationType type) {

        if (repository.existsByChatId(chatId)) {
            log.debug(String.format("*** chatId [%d] is already exists", chatId));
            return;
        }

        NotificationChatEntity entity = new NotificationChatEntity();
        entity.setChatId(chatId);
        entity.setType(type);
        repository.save(entity);
    }

    public List<Long> getAllByNotificationType(TelegramNotificationType type) {
        return repository.findAllByType(type).stream()
                .map(NotificationChatEntity::getChatId)
                .collect(Collectors.toList());
    }
}
