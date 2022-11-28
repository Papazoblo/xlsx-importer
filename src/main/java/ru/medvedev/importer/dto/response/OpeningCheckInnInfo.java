package ru.medvedev.importer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.medvedev.importer.enums.OpeningInnStatus;

@Data
public class OpeningCheckInnInfo {

    private String inn;
    @JsonProperty("inn_status")
    private OpeningInnStatus innStatus;
    private String message;
}
