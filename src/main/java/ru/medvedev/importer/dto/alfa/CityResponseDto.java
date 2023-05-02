package ru.medvedev.importer.dto.alfa;

import lombok.Data;
import ru.medvedev.importer.dto.CityDto;

import java.util.ArrayList;
import java.util.List;

@Data
public class CityResponseDto {
    private String id;
    private String code;
    private String name;
    private List<CityInfoDto> values = new ArrayList<>();
}
