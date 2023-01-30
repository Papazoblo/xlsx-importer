package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Optional;

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

    public static Bank ofScenario(String value) {
        String scenarioName = Optional.ofNullable(value).map(String::toLowerCase).orElse("");
        return Arrays.stream(Bank.values())
                .filter(bank -> scenarioName.contains(bank.getTitle().toLowerCase()))
                .findFirst().orElse(null);
    }
}
