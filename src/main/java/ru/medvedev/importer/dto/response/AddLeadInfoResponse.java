package ru.medvedev.importer.dto.response;

import lombok.Data;
import ru.medvedev.importer.enums.AddLeadStatus;

@Data
public class AddLeadInfoResponse {

    private Long leadId;
    private AddLeadStatus status;
    private String responseCode;
    private String responseCodeDescription;
}
