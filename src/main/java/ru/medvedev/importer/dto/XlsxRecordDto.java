package ru.medvedev.importer.dto;

import lombok.Data;

@Data
public class XlsxRecordDto {

    private String fio;
    private String phone;
    private String email;
    private String city;
    private String inn;
    private String region;
    private String position;
    private String description;
    private String[] tags;
    private String orgName;
    private String orgPhone;
    private String orgEmail;
    private String orgHost;
    private String orgCity;
    private String orgAddress;
    private String orgRegion;
    private String orgActivity;
    private String orgInn;
    private String orgKpp;
    private String[] orgTags;
    private String orgDescription;
}
