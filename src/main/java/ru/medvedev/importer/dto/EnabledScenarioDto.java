package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.enums.Bank;

@Data
public class EnabledScenarioDto {

    private Long id;
    private Long scenarioId;
    private Bank bank;
}
