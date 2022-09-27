package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;

@RequiredArgsConstructor
public enum Bank {

    VTB("ВТБ"),
    VTB_OPENING("Открытие");

    @Getter
    private final String title;

    public static Bank of(String title) {
        return Arrays.stream(Bank.values())
                .filter(item -> item.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .orElseThrow(EntityNotFoundException::new);
    }
}
