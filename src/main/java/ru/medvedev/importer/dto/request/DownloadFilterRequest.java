package ru.medvedev.importer.dto.request;

import lombok.Data;
import ru.medvedev.importer.dto.response.DownloadFilterDto;

import java.util.ArrayList;
import java.util.List;

@Data
public class DownloadFilterRequest {

    private List<DownloadFilterDto> filter = new ArrayList<>();
}
