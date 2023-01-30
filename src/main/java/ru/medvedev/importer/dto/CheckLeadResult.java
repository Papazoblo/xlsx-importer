package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.dto.response.LeadInfoResponse;

import java.util.ArrayList;
import java.util.List;

@Data
public class CheckLeadResult {

    private Boolean status;
    private String additionalInfo;
    private List<LeadInfoResponse> leadResponse = new ArrayList<>();

    public static CheckLeadResult of(Boolean status, String additionalInfo, List<LeadInfoResponse> leads) {
        CheckLeadResult result = new CheckLeadResult();
        result.setAdditionalInfo(additionalInfo);
        result.setStatus(status);
        result.setLeadResponse(leads);
        return result;
    }

    public static CheckLeadResult of(Boolean status) {
        return of(status, null, null);
    }
}
