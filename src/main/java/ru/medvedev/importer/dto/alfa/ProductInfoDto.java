package ru.medvedev.importer.dto.alfa;

import lombok.Data;
import ru.medvedev.importer.enums.BankProduct;

import java.util.Collections;
import java.util.List;

@Data
public class ProductInfoDto {
    private BankProduct productCode = BankProduct.LP_RKO;
}
