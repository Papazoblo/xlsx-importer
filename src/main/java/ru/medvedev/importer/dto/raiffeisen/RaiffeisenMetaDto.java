package ru.medvedev.importer.dto.raiffeisen;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RaiffeisenMetaDto {

    @JsonProperty("partnerID")
    private String partnerId;
    private String businessProduct;
    @JsonProperty("refID")
    private String refId;
}
