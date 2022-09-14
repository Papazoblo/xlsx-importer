package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;

@RequiredArgsConstructor
public enum XlsxRequireField {

    NAME("Имя"),
    SURNAME("Фамилия"),
    MIDDLE_NAME("Отчество"),
    FIO("ФИО"),
    ORG_NAME("Наименование ЮЛ"),
    PHONE("Телефон"),
    INN("ИНН"),
    OGRN("ОГРН"),
    CITY("Город"), //todo бывший адрес
    TRASH("Мусорный столбец");

    @Getter
    private final String description;

    public static XlsxRequireField of(String name) {
        if (name.equals("Пропустить")) {
            return TRASH;
        }
        return Arrays.stream(XlsxRequireField.values())
                .filter(field -> field != TRASH)
                .filter(field -> field.getDescription().equals(name))
                .findFirst().orElseThrow(EntityNotFoundException::new);
    }
}
