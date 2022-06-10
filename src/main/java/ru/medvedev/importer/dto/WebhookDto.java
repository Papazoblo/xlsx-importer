package ru.medvedev.importer.dto;

import lombok.Data;

@Data
public class WebhookDto {

    private String type;
    private LeadDto contact;
}
