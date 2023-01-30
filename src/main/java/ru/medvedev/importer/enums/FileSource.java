package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FileSource {

    TELEGRAM("Телеграм"),
    UI("Интерфейс"),
    UI_REDOWNLOAD("Интерфейс(повторная загрузка)");

    @Getter
    private final String description;
}
