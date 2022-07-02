package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ContactStatus {

    ADDED("Добавлен"),
    IN_PROCESS("В обработке"), // возможно лишний статус
    DOWNLOADED("Загружен"), // загружен
    REJECTED("Отклонен"); // отменен ВТБ или чем то еще

    @Getter
    private final String description;
}
