package ru.medvedev.importer.dto.raiffeisen;

import lombok.Data;

@Data
public class RaiffeisenRequestCreateLeadDto {

    private RaiffeisenMetaDto meta;
    private RaiffeisenCreateLeadInfoDto data;
}
