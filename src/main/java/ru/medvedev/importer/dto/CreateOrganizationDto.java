package ru.medvedev.importer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateOrganizationDto {

    @JsonProperty("name")
    private String name;
    private List<String> phones;
    private List<String> emails;
    private String homepage;
    private String city;
    private String address;
    private String region;
    private String business;
    private String inn;
    private String kpp;
    private String comment;
    private List<CreateLeadDto> leads = new ArrayList<>();
    private String type = "OrganizationLead";
}
