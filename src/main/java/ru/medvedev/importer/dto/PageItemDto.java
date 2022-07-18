package ru.medvedev.importer.dto;

import lombok.Builder;
import lombok.Data;
import ru.medvedev.importer.enums.PageItemType;

@Data
@Builder
public class PageItemDto {

    private PageItemType pageItemType;

    private int index;

    private boolean active;
}
