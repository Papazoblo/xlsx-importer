package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ContactStatus {

    ADDED("Добавлен"),
    IN_PROCESS("В обработке"),
    DOWNLOADED("Загружен"),
    REJECTED("Отклонен");

    @Getter
    private final String description;
}
