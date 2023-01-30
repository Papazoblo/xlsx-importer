package ru.medvedev.importer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WebhookDto {

    private String type;
    private WebhookLeadDto lead;
    private WebhookContactDto contact;

    private CallDto call;

    @JsonProperty("call_result")
    private CallResultDto callResult;
}
