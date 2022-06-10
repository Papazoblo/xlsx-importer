package ru.medvedev.importer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateOrganizationDto {

    @JsonProperty("firm_name")
    private String firmName;
    private List<String> phones;
    private List<String> emails;
    private String homepage;
    private String city;
    private String address;
    private String region;
    private String business;
    private String inn;
    private String kpp;
    private String description;
    private List<String> tags;
    private List<CreateLeadDto> leads = new ArrayList<>();
}
