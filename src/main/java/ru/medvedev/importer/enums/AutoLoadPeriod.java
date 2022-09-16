package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AutoLoadPeriod {

    EVERY_DAY("Каждый день"),
    IN_ONE_DAY("Через день"),
    EVERY_MONDAY("Каждый понедельник"),
    EVERY_FIRST_DAY_OF_MONTH("Каждый 1-ый день месяца");

    @Getter
    private final String title;
}
