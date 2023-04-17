package ru.medvedev.importer.dto.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.medvedev.importer.enums.EventType;
import ru.medvedev.importer.enums.TelegramNotificationType;

@Getter
public class NotificationEvent extends ApplicationEvent {

    private final String description;
    private final EventType eventType;
    private final TelegramNotificationType type;
    private final boolean withCancelButton;

    public NotificationEvent(Object source, String description, TelegramNotificationType type, EventType eventType, boolean withCancelButton) {
        super(source);
        this.description = description;
        this.eventType = eventType;
        this.type = type;
        this.withCancelButton = withCancelButton;
    }

    public NotificationEvent(Object source, String description, TelegramNotificationType type, EventType eventType) {
        this(source, description, type, eventType, false);
    }
}
