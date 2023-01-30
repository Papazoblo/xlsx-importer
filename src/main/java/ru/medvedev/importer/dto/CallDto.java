package ru.medvedev.importer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CallDto {

    @JsonProperty("scenario_id")
    private Long scenarioId;
}
