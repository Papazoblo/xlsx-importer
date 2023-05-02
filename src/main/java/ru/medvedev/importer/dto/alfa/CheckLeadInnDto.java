package ru.medvedev.importer.dto.alfa;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CheckLeadInnDto {
    private OrganizationInfoDto organizationInfo;
    private List<ContactInfoDto> contactInfo = new ArrayList<>();
    private List<ProductInfoDto> productInfo = new ArrayList<>();
}
