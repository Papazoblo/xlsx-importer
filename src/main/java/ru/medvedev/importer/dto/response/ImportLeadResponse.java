package ru.medvedev.importer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ImportLeadResponse {

    private Long id;

    private String state;

    @JsonProperty("total_count")
    private Long totalCount;
}
