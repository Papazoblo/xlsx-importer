package ru.medvedev.importer.dto;

import lombok.Data;

@Data
public class CreateLeadResult {

    private Boolean status;
    private String additionalInfo;

    public static CreateLeadResult of(Boolean status, String additionalInfo) {
        CreateLeadResult result = new CreateLeadResult();
        result.setAdditionalInfo(additionalInfo);
        result.setStatus(status);
        return result;
    }

    public static CreateLeadResult of(Boolean status) {
        return of(status, null);
    }
}
