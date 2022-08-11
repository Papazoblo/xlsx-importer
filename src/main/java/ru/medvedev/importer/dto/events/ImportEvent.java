package ru.medvedev.importer.dto.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.medvedev.importer.enums.EventType;

@Getter
public class ImportEvent extends ApplicationEvent {

    private final String description;
    private final EventType eventType;
    private final Long fileId;
    private final boolean withCancelButton;

    public ImportEvent(Object source, String description, EventType eventType, Long fileId, boolean withCancelButton) {
        super(source);
        this.description = description;
        this.eventType = eventType;
        this.fileId = fileId;
        this.withCancelButton = withCancelButton;
    }

    public ImportEvent(Object source, String description, EventType eventType, Long fileId) {
        this(source, description, eventType, fileId, false);
    }
}
