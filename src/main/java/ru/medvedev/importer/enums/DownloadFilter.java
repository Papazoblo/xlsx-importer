package ru.medvedev.importer.enums;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public enum DownloadFilter {

    INN("Исключить ИНН", "Контакт с ИНН, который будет начинаться на указанные цифры будет пропущен"),
    PHONE("Исключить телефон", "Контакт с телефоном, который будет начинаться на указанные цифры будет пропущен");

    @Getter
    private final String title;
    @Getter
    private final String description;
}
