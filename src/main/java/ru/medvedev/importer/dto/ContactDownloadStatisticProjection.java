package ru.medvedev.importer.dto;

import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.ContactStatus;

public interface ContactDownloadStatisticProjection {

    ContactStatus getStatus();

    Bank getBank();

    Integer getCount();
}
