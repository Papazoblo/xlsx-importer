package ru.medvedev.importer.dto;

import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.RequestStatus;

public interface RequestStatisticProjection {

    RequestStatus getStatus();

    Bank getBank();

    Integer getCount();
}
