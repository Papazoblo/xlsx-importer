package ru.medvedev.importer.dto.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SaveMessageIdEvent extends ApplicationEvent {

    private final Integer messageId;
    private final Long fileId;

    public SaveMessageIdEvent(Object source, Integer messageId, Long fileId) {
        super(source);
        this.messageId = messageId;
        this.fileId = fileId;
    }
}
