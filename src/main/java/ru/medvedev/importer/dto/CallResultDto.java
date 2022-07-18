package ru.medvedev.importer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CallResultDto {

    @JsonProperty("result_name")
    private String resultName;
}
