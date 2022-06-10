package ru.medvedev.importer.dto.response;

import lombok.Data;
import ru.medvedev.importer.enums.CheckLeadStatus;

@Data
public class LeadInfoResponse {

    private CheckLeadStatus responseCode;
    private String responseCodeDescription;
    private String inn;
}
