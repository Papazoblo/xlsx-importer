package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.NotificationChatEntity;

@Repository
public interface NotificationChatRepository extends JpaRepository<NotificationChatEntity, Long> {

    boolean existsByChatId(Long chatId);
}
