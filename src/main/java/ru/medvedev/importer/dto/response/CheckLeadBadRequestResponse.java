package ru.medvedev.importer.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import ru.medvedev.importer.dto.LeadDto;

import java.util.ArrayList;
import java.util.List;

@Data
public class CheckLeadBadRequestResponse {

    @JsonIgnore
    private String moreInformation;
    private List<LeadDto> leads = new ArrayList<>();
}
