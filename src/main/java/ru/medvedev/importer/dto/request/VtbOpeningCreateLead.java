package ru.medvedev.importer.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VtbOpeningCreateLead {

    @JsonProperty("full_name")
    private String full_name;
    private String inn;
    private String email;
    private String phone;
    private String city;
}
