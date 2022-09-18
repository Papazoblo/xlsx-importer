package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Bank {

    VTB("ВТБ"),
    VTB_OPENING("Открытие");

    @Getter
    private final String title;
}
