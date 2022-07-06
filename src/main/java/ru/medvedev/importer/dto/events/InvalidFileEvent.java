package ru.medvedev.importer.dto.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class InvalidFileEvent extends ApplicationEvent {

    private final Long fileId;

    public InvalidFileEvent(Object source, Long fileId) {
        super(source);
        this.fileId = fileId;
    }
}
