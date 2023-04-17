package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.TelegramNotificationType;

import javax.persistence.*;

@Entity
@Table(name = "notification_chat")
@Data
public class NotificationChatEntity {

    @Id
    @SequenceGenerator(name = "notificationChatIdGen", sequenceName = "notification_chat_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "notificationChatIdGen", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private TelegramNotificationType type = TelegramNotificationType.ALL;
}
