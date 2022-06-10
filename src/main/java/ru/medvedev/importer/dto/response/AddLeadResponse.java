package ru.medvedev.importer.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AddLeadResponse {

    private List<AddLeadInfoResponse> leads = new ArrayList<>();
}
