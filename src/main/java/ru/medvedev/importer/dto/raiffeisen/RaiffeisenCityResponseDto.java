package ru.medvedev.importer.dto.raiffeisen;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RaiffeisenCityResponseDto {

    private List<RaiffeisenCityDto> data = new ArrayList<>();
}
