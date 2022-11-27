package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OpeningRequestStatus {

    CREATING("Создание"),
    IN_QUEUE("Ожидают"),
    CHECKING("Проверяются"),
    SUCCESS_CHECK("Проверены"),
    DOWNLOADING("Загружаются"),
    DOWNLOADED("Загружены"),
    ERROR("Завершены с ошибкой");

    @Getter
    private final String title;
}
