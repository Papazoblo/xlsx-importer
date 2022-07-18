package ru.medvedev.importer.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CheckLeadResponse {

    private List<LeadInfoResponse> leads = new ArrayList<>();
}
