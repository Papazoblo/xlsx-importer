package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.entity.PartsLoadEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.PartsLoadStatus;

import javax.persistence.Table;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
public class PartsLoadDto {

    private Long id;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private Bank bank;
    private Integer count;
    private LocalDateTime createDate;
    private PartsLoadStatus status;
    private Integer timeZone;

    public static PartsLoadDto of(PartsLoadEntity entity) {
        PartsLoadDto dto = new PartsLoadDto();
        dto.setId(entity.getId());
        dto.setCount(entity.getCount());
        dto.setStatus(entity.getStatus());
        dto.setDateTo(entity.getDateTo());
        dto.setBank(entity.getBank());
        dto.setDateFrom(entity.getDateFrom());
        dto.setCreateDate(entity.getCreateDate());
        return dto;
    }
}
