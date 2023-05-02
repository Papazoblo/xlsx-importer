package ru.medvedev.importer.dto.alfa;

import lombok.Data;

@Data
public class CheckErrorDto {
    private Integer status;
    private String code;
    private String title;
    private String detail;
}
