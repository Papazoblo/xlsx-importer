package ru.medvedev.importer.enums;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public enum DownloadFilter {

    INN("Исключить ИНН", "ИНН контакта, который будет начинаться на указанные цифры будет пропущен", new TypeReference<List<String>>() {
    }, Collections.emptyList()),
    abs("Исключить ИНН", "ИНН контакта, который будет начинаться на указанные цифры будет пропущен", new TypeReference<List<String>>() {
    }, Collections.emptyList()),
    asd("Исключить ИНН", "ИНН контакта, который будет начинаться на указанные цифры будет пропущен", new TypeReference<List<String>>() {
    }, Collections.emptyList()),
    sgdfgdf("Исключить ИНН", "ИНН контакта, который будет начинаться на указанные цифры будет пропущен", new TypeReference<List<String>>() {
    }, Collections.emptyList());

    @Getter
    private final String title;
    @Getter
    private final String description;
    @Getter
    private final TypeReference<?> type;
    @Getter
    private final Object defaultValue;
}
