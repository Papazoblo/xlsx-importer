package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ContactActuality {

    NEW("Новый"),
    PROCESSED("Обработан");

    @Getter
    private final String title;
}
