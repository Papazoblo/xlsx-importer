package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.enums.PageItemType;

import java.util.ArrayList;
import java.util.List;

@Data
public class PagingDto {

    private static final int PAGINATION_STEP = 3;

    private boolean nextEnabled;
    private boolean prevEnabled;
    private int pageSize;
    private int pageNumber;

    private List<PageItemDto> items = new ArrayList<>();

    public void addPageItems(int from, int to, int pageNumber) {
        for (int i = from; i < to; i++) {
            items.add(PageItemDto.builder()
                    .active(pageNumber != i)
                    .index(i)
                    .pageItemType(PageItemType.PAGE)
                    .build());
        }
    }

    public void last(int pageSize) {
        items.add(PageItemDto.builder()
                .active(false)
                .pageItemType(PageItemType.DOTS)
                .build());

        items.add(PageItemDto.builder()
                .active(true)
                .index(pageSize)
                .pageItemType(PageItemType.PAGE)
                .build());
    }

    public void first(int pageNumber) {
        items.add(PageItemDto.builder()
                .active(pageNumber != 1)
                .index(1)
                .pageItemType(PageItemType.PAGE)
                .build());

        items.add(PageItemDto.builder()
                .active(false)
                .pageItemType(PageItemType.DOTS)
                .build());
    }

    public static PagingDto of(int totalPages, int pageNumber, int pageSize) {
        PagingDto paging = new PagingDto();
        paging.setPageSize(pageSize);
        paging.setNextEnabled(pageNumber != totalPages);
        paging.setPrevEnabled(pageNumber != 1);
        paging.setPageNumber(pageNumber);

        if (totalPages < PAGINATION_STEP * 2 + 6) {
            paging.addPageItems(1, totalPages + 1, pageNumber);

        } else if (pageNumber < PAGINATION_STEP * 2 + 1) {
            paging.addPageItems(1, PAGINATION_STEP * 2 + 4, pageNumber);
            paging.last(totalPages);

        } else if (pageNumber > totalPages - PAGINATION_STEP * 2) {
            paging.first(pageNumber);
            paging.addPageItems(totalPages - PAGINATION_STEP * 2 - 2, totalPages + 1, pageNumber);

        } else {
            paging.first(pageNumber);
            paging.addPageItems(pageNumber - PAGINATION_STEP, pageNumber + PAGINATION_STEP + 1, pageNumber);
            paging.last(totalPages);
        }

        return paging;
    }
}
