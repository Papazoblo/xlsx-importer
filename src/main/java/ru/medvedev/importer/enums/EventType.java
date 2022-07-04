package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {

    ACTION("Событие"),
    ERROR("Ошибка"),
    NOTIFICATION("Уведомление");

    private final String description;
}
