package ru.medvedev.importer.dto.request;

import lombok.Data;
import ru.medvedev.importer.enums.DownloadFilter;

@Data
public class DownloadFilterRequest {

    private DownloadFilter name;
    private Object filter;
}
