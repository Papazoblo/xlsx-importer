package ru.medvedev.importer.dto.response;

import lombok.Data;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.DownloadFilter;

@Data
public class DownloadFilterDto {

    private Long id;
    private DownloadFilter code;
    private String value;
    private Bank bank;
}
