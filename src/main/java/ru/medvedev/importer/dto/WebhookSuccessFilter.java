package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.WebhookType;

@Data
public class WebhookSuccessFilter {

    private String name;
    private Bank bank;
    private WebhookType type;
}
