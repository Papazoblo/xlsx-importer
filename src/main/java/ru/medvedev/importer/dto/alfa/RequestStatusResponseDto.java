package ru.medvedev.importer.dto.alfa;

import lombok.Data;
import ru.medvedev.importer.enums.AlfaProductStatus;

import java.util.ArrayList;
import java.util.List;

@Data
public class RequestStatusResponseDto {

    private String uuid;
    private AlfaProductStatus state;
    private String status;
    private String detailedStatus;
    private List<ProductInfoStatusDto> productInfo = new ArrayList<>();
}
