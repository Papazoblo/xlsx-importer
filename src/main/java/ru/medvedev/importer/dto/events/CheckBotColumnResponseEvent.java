package ru.medvedev.importer.dto.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CheckBotColumnResponseEvent extends ApplicationEvent {

    private final String text;

    public CheckBotColumnResponseEvent(Object source, String text) {
        super(source);
        this.text = text;
    }

    public Integer getPosition() {
        return Integer.valueOf(text.split("\\.")[0]);
    }
}
