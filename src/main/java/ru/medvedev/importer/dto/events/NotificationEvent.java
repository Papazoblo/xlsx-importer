package ru.medvedev.importer.dto.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.medvedev.importer.enums.EventType;

@Getter
public class NotificationEvent extends ApplicationEvent {

    private final String description;
    private final EventType eventType;
    private final boolean withCancelButton;

    public NotificationEvent(Object source, String description, EventType eventType, boolean withCancelButton) {
        super(source);
        this.description = description;
        this.eventType = eventType;
        this.withCancelButton = withCancelButton;
    }

    public NotificationEvent(Object source, String description, EventType eventType) {
        this(source, description, eventType, false);
    }
}
