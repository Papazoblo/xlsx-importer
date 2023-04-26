package ru.medvedev.importer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CallResultDto {

    @JsonProperty("result_name")
    private String resultName;
    private String comment;
}
