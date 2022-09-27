package ru.medvedev.importer.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.ContactStatus;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

@Data
public class ContactFilter {

    private String orgName;
    private String name;
    private String surname;
    private String middleName;
    private String phone;
    private String inn;
    private String ogrn;
    private String region;
    private String city;
    private List<ContactStatus> status = new ArrayList<>();
    private List<Bank> bank = new ArrayList<>();
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createDateFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createDateTo;
    private List<Boolean> original = new ArrayList<>();

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

    public boolean isInOriginal(Boolean val) {
        return original != null && original.contains(val);
    }

    public String getUrlString() throws URISyntaxException {
        StringBuilder builder = new StringBuilder("?");
        builder.append("orgName=");
        builder.append(Optional.ofNullable(orgName).orElse(""));
        builder.append("&name=");
        builder.append(Optional.ofNullable(name).orElse(""));
        builder.append("&surname=");
        builder.append(Optional.ofNullable(surname).orElse(""));
        builder.append("&middleName=");
        builder.append(Optional.ofNullable(middleName).orElse(""));
        builder.append("&phone=");
        builder.append(Optional.ofNullable(phone).orElse(""));
        builder.append("&inn=");
        builder.append(Optional.ofNullable(inn).orElse(""));
        builder.append("&ogrn=");
        builder.append(Optional.ofNullable(ogrn).orElse(""));
        builder.append("&region=");
        builder.append(Optional.ofNullable(region).orElse(""));
        builder.append("&createDateTo=");
        builder.append(Optional.ofNullable(createDateTo).map(LocalDateTime::toString).orElse(""));
        builder.append("&createDateFrom=");
        builder.append(Optional.ofNullable(createDateFrom).map(LocalDateTime::toString).orElse(""));
        builder.append("&status=");
        builder.append(status.stream().map(ContactStatus::name).collect(joining(",")));
        builder.append("&bank=");
        builder.append(bank.stream().map(Bank::name).collect(joining(",")));
        builder.append("&original=");
        builder.append(original.stream().map(String::valueOf).collect(joining(",")));
        return builder.toString();
    }
}
