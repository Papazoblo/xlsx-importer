package ru.medvedev.importer.dto.alfa;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrganizationInfoDto {
    private String organizationName;
    private String inn;
}
