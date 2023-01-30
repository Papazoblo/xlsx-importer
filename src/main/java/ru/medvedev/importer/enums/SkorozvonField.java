package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public enum SkorozvonField {

    USR_FIO("ФИО", true, FieldType.SELECT, "польз."),
    USR_PHONE("Телефон", true, FieldType.SELECT, "польз."),
    USR_EMAIL("E-mail", false, FieldType.SELECT, "польз."),
    USR_CITY("Город", false, FieldType.SELECT, "польз."),
    //USR_INN("ИНН", true, FieldType.SELECT),
    USR_ADDRESS("Адрес", false, FieldType.SELECT, "польз."),
    USR_REGION("Регион", false, FieldType.SELECT, "польз."),
    USR_POSITION("Должность", false, FieldType.SELECT, "польз."),
    USR_DESCRIPTION("Описание", false, FieldType.SELECT, "польз."),
    //USR_TAGS("Теги, разделитель ';'", false, FieldType.INPUT),

    SPACE("", false, FieldType.SPACE, ""),

    ORG_NAME("Название", false, FieldType.SELECT, "орг."),
    ORG_PHONE("Телефон", true, FieldType.SELECT, "орг."),
    ORG_EMAIL("E-mail", false, FieldType.SELECT, "орг."),
    ORG_HOST("Сайт", false, FieldType.SELECT, "орг."),
    ORG_CITY("Город", false, FieldType.SELECT, "орг."),
    ORG_ADDRESS("Адрес", false, FieldType.SELECT, "орг."),
    ORG_REGION("Регион", false, FieldType.SELECT, "орг."),
    ORG_ACTIVITY("Сфера деятельности", false, FieldType.SELECT, "орг."),
    ORG_INN("ИНН", true, FieldType.SELECT, "орг."),
    ORG_KPP("КПП", false, FieldType.SELECT, "орг."),
    USD_DESCRIPTION("Описание", false, FieldType.SELECT, "орг."),
    ORG_TAGS("Теги, разделитель ';'", false, FieldType.MULTIPLE, "");

    private final String description;
    private final boolean required;
    private final FieldType fieldType;
    private final String fullDescription;

    public static List<SkorozvonField> selectValues() {
        return Arrays.stream(values())
                .filter(item -> item.getFieldType() == FieldType.SELECT)
                .collect(Collectors.toList());
    }
}
