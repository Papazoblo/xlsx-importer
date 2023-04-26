package ru.medvedev.importer.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.ContactStatus;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.util.stream.Collectors.joining;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Data
public class InnExcludeFilter {

    private Bank bank;
    private String inn;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createDateFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createDateTo;

    public String getDateFromString() {
        return Optional.ofNullable(createDateFrom).map(value ->
                        value.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                .orElse("");
    }

    public String getDateToString() {
        return Optional.ofNullable(createDateTo).map(value ->
                        value.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                .orElse("");
    }

    public String getUrlString() {
        StringBuilder builder = new StringBuilder("?");

        Optional.ofNullable(bank).ifPresent(item -> builder.append("bank=").append(item.name()));
        Optional.ofNullable(inn).ifPresent(item -> builder.append("inn=").append(inn));
        Optional.ofNullable(createDateFrom).ifPresent(item -> builder.append("createDateFrom=").append(createDateFrom));
        Optional.ofNullable(createDateTo).ifPresent(item -> builder.append("createDateTo=").append(createDateTo));
        return builder.toString();
    }
}
