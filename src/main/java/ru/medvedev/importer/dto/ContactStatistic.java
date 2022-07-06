package ru.medvedev.importer.dto;

import ru.medvedev.importer.enums.ContactStatus;

public interface ContactStatistic {

    Long getCount();

    ContactStatus getStatus();
}
