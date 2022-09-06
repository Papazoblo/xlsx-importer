package ru.medvedev.importer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookLeadDto {

    private String phones;
    private String city;
    private String inn;
    private String name;
}
