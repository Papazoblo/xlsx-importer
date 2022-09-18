package ru.medvedev.importer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.medvedev.importer.enums.VtbOpeningInnStatus;

@Data
public class VtbOpeningCheckInnInfo {

    private String inn;
    @JsonProperty("inn_status")
    private VtbOpeningInnStatus innStatus;
    private String message;
}
