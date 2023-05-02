package ru.medvedev.importer.dto.alfa;

import lombok.Data;
import org.springframework.boot.convert.DataSizeUnit;

@Data
public class CityInfoDto {

    private String name;
    private String id;
    private String fias;
}
