package ru.medvedev.importer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import ru.medvedev.importer.enums.CheckLeadStatus;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeadDto {

    private String phone;
    private String city;
    private String inn;
    private Boolean consentOnPersonalDataProcessing;
    private String productCode = "Payments";
    private String responseCodeDescription;
    private CheckLeadStatus responseCode;
}
