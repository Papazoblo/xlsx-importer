package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FileSource {

    TELEGRAM("Телеграм"),
    UI("Интерфейс");

    @Getter
    private final String description;
}
