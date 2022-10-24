package ru.medvedev.importer.dto;

import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.OpeningRequestStatus;

public interface RequestStatisticProjection {

    OpeningRequestStatus getStatus();

    Bank getBank();

    Integer getCount();
}
