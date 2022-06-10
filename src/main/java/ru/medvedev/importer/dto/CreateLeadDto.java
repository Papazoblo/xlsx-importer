package ru.medvedev.importer.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateLeadDto {

    private String name;
    private List<String> phones;
    private List<String> emails;
    private String city;
    private String inn;
    private String region;
    private String post;
    private String description;
    private List<String> tags;
}
