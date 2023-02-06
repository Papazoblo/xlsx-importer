package ru.medvedev.importer.dto.raiffeisen;

import lombok.Data;

@Data
public class RaiffeisenPhoneDto {

    private String countryCode;
    private String phoneNumber;
}
