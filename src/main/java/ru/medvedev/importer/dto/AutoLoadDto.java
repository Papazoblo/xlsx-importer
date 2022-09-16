package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.enums.AutoLoadPeriod;

import java.time.LocalDate;

@Data
public class AutoLoadDto {

    private Long id;
    private Integer interval;
    private AutoLoadPeriod period;
    private ContactFilter filter;
    private LocalDate lastLoad;
    private LocalDate createDate;
    private Long projectId;
    private Boolean enabled;
}
