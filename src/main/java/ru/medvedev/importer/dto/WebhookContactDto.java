package ru.medvedev.importer.dto;

import lombok.Data;

@Data
public class WebhookContactDto {

    private Long id;
    private String name;
    private String comment;
}
