package ru.medvedev.importer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookContactDto {

    private Long id;
    private String name;
    private String comment;
}
