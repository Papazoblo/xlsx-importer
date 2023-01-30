package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {

    LOG("Лог"),
    LOG_TG("Лог"),
    FILE_PROCESS("Информация по обработке"),
    ERROR("Ошибка"),
    SUCCESS("Успешная обработка");

    private final String description;
}
