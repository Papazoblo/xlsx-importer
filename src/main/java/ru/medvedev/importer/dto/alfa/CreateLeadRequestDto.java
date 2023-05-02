package ru.medvedev.importer.dto.alfa;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateLeadRequestDto {

    private OrganizationInfoDto organizationInfo;
    private List<ContactInfoDto> contactInfo;
    private RequestInfoDto requestInfo;
    private List<ProductInfoDto> productInfo = new ArrayList<>();
}
