package ru.medvedev.importer.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.ContactStatus;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

@Data
public class ContactCountDateStatisticFilter {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;
    private int offset = 1;
    private int limit = 15;

    public String getDateFromString() {
        return dateFrom == null ? "" : dateFrom.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public String getDateToString() {
        return dateTo == null ? "" : dateTo.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public String getUrlString() {
        StringBuilder builder = new StringBuilder("?");
        builder.append("&dateFrom=");
        builder.append(Optional.ofNullable(dateFrom).map(LocalDate::toString).orElse(""));
        builder.append("&dateTo=");
        builder.append(Optional.ofNullable(dateTo).map(item -> item.plusDays(1).toString()).orElse(""));
        return builder.toString();
    }
}
