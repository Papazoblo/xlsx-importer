package ru.medvedev.importer.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SkorozvonField {

    USR_FIO("ФИО", true),
    USR_PHONE("Телефон", true),
    USR_EMAIL("E-mail", false),
    USR_CITY("Город", false),
    USR_INN("ИНН", true),
    USR_REGION("Регион", false),
    USR_POSITION("Должность", false),
    USR_DESCRIPTION("Описание", false),
    USR_TAGS("Теги", false),

    ORG_NAME("Название", false),
    ORG_PHONE("Телефон", false),
    ORG_EMAIL("E-mail", false),
    ORG_HOST("Сайт", false),
    ORG_CITY("Город", false),
    ORG_ADDRESS("Адрес", false),
    ORG_REGION("Регион", false),
    ORG_ACTIVITY("Сфера деятельности", false),
    ORG_INN("ИНН", false),
    ORG_KPP("КПП", false),
    ORG_TAGS("Теги", false),
    USD_DESCRIPTION("Описание", false);

    private final String name;
    private final boolean required;
}
