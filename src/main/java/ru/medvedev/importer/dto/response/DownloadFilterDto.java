package ru.medvedev.importer.dto.response;

import lombok.Data;
import ru.medvedev.importer.enums.DownloadFilter;

@Data
public class DownloadFilterDto {

    private Long id;
    private DownloadFilter name;
    private Object filter;
}
