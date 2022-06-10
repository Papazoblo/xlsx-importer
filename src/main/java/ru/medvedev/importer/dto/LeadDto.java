package ru.medvedev.importer.dto;

import lombok.Data;

@Data
public class LeadDto {


    private String phone;
    private String city;
    private String inn;
    private boolean consentOnPersonalDataProcessing = true;
    private String productCode = "Payments";
}
