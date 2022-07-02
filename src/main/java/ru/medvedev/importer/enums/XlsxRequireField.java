package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum XlsxRequireField {

    NAME("Имя"),
    SURNAME("Фамилия"),
    MIDDLE_NAME("Отчество"),
    ORG_NAME("Наименование ЮЛ"),
    PHONE("Телефон"),
    INN("ИНН"),
    OGRN("ОГРН"),
    ADDRESS("Адрес");

    @Getter
    private final String description;
}
