package ru.medvedev.importer.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.medvedev.importer.dto.CreateOrganizationDto;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateMultipleRequest {

    @JsonProperty("call_project_id")
    private Long callProjectId;
    private List<String> tags;
    private List<CreateOrganizationDto> data = new ArrayList<>();
}
