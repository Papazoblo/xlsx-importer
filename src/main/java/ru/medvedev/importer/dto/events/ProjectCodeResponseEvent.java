package ru.medvedev.importer.dto.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProjectCodeResponseEvent extends ApplicationEvent {

    private final String text;

    public ProjectCodeResponseEvent(Object source, String text) {
        super(source);
        this.text = text;
    }
}
