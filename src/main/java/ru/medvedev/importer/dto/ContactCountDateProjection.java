package ru.medvedev.importer.dto;

import java.time.LocalDate;

public interface ContactCountDateProjection {

    int getCount();

    LocalDate getDate();
}
