package ru.medvedev.importer.dto.alfa;

import lombok.Data;

@Data
public class RequestInfoDto {

    private String avdCode = "db_fab";
    private String comment;
    private String cityCode;
}
