package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ContactStatus {

    ADDED("Дубликаты"),
    DOWNLOADED("Загружено в скорозвон"), // загружен
    REJECTED("Отклонен ВТБ"); // отменен ВТБ или чем то еще

    @Getter
    private final String description;
}
