package ru.medvedev.importer.dto.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CompleteFileEvent extends ApplicationEvent {

    private final Long fileId;

    public CompleteFileEvent(Object source, Long fileId) {
        super(source);
        this.fileId = fileId;
    }
}
