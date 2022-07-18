package ru.medvedev.importer.dto.request;

import lombok.Data;
import ru.medvedev.importer.dto.LeadDto;

import java.util.ArrayList;
import java.util.List;

@Data
public class LeadRequest {

    private List<LeadDto> leads = new ArrayList<>();
}
