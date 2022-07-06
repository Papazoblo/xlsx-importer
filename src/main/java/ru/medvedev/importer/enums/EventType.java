package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {

    LOG("Лог события"),
    ERROR("Ошибка"),
    SUCCESS("Успешная обработка");

    private final String description;
}
