package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.NotificationChatEntity;
import ru.medvedev.importer.enums.TelegramNotificationType;

import java.util.List;

@Repository
public interface NotificationChatRepository extends JpaRepository<NotificationChatEntity, Long> {

    boolean existsByChatId(Long chatId);

    List<NotificationChatEntity> findAllByType(TelegramNotificationType type);
}
