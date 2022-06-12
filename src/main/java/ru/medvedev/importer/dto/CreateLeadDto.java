package ru.medvedev.importer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateLeadDto {

    private String name;
    private List<String> phones;
    private List<String> emails;
    private String city;
    private String address;
    private String region;
    private String post;
    private String comment;
}
