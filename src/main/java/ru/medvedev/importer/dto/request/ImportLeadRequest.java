package ru.medvedev.importer.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.medvedev.importer.dto.CreateLeadDto;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImportLeadRequest {

    @JsonProperty("call_project_id")
    private Long callProjectId;
    private List<String> tags;
    private List<CreateLeadDto> data = new ArrayList<>();
}
