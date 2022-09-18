package ru.medvedev.importer.dto;

import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.WebhookStatus;

public interface DailyContactStatistic {

    Long getCount();

    WebhookStatus getStatus();

    Bank getBank();
}
