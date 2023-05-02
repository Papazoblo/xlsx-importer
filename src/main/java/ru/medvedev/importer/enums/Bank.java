package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum Bank {

    RAIFFEISEN("Райффайзен", true, 100),
    VTB("ВТБ", false, 100),
    VTB_OPENING("Открытие", false, 100),
    ALFA("Альфа", false, 5);

    @Getter
    private final String title;
    @Getter
    private final boolean exclude;
    @Getter
    private final int batchSize;

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

    public static Bank[] valuesNotExclude() {
        return Arrays.stream(Bank.values())
                .filter(item -> !item.isExclude())
                .toArray(Bank[]::new);
    }
}
