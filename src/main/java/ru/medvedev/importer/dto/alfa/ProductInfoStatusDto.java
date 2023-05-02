package ru.medvedev.importer.dto.alfa;

import lombok.Data;
import ru.medvedev.importer.enums.AlfaProductStatus;

import java.util.Collections;
import java.util.List;

@Data
public class ProductInfoStatusDto {
    private String productCode;
    private AlfaProductStatus state;
    private String status;
}
