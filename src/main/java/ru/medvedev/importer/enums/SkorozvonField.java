package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SkorozvonField {

    USR_FIO("ФИО", true, FieldType.SELECT),
    USR_PHONE("Телефон", true, FieldType.SELECT),
    USR_EMAIL("E-mail", false, FieldType.SELECT),
    USR_CITY("Город", false, FieldType.SELECT),
    //USR_INN("ИНН", true, FieldType.SELECT),
    USR_ADDRESS("Адрес", false, FieldType.SELECT),
    USR_REGION("Регион", false, FieldType.SELECT),
    USR_POSITION("Должность", false, FieldType.SELECT),
    USR_DESCRIPTION("Описание", false, FieldType.SELECT),
    //USR_TAGS("Теги, разделитель ';'", false, FieldType.INPUT),

    SPACE("", false, FieldType.SPACE),

    ORG_NAME("Название", false, FieldType.SELECT),
    ORG_PHONE("Телефон", true, FieldType.SELECT),
    ORG_EMAIL("E-mail", false, FieldType.SELECT),
    ORG_HOST("Сайт", false, FieldType.SELECT),
    ORG_CITY("Город", false, FieldType.SELECT),
    ORG_ADDRESS("Адрес", false, FieldType.SELECT),
    ORG_REGION("Регион", false, FieldType.SELECT),
    ORG_ACTIVITY("Сфера деятельности", false, FieldType.SELECT),
    ORG_INN("ИНН", true, FieldType.SELECT),
    ORG_KPP("КПП", false, FieldType.SELECT),
    USD_DESCRIPTION("Описание", false, FieldType.SELECT),
    ORG_TAGS("Теги, разделитель ';'", false, FieldType.MULTIPLE);

    private final String description;
    private final boolean required;
    private final FieldType fieldType;
}
