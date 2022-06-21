package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FileStatus {

    DOWNLOADED("Загружен"),
    IN_PROCESS("В обработке"),
    PROCESSED("Обработан");

    @Getter
    private final String description;
}
