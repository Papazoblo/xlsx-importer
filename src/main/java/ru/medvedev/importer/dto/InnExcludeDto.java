package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.entity.InnExcludeEntity;
import ru.medvedev.importer.entity.id.InnExcludeId;
import ru.medvedev.importer.enums.Bank;

import javax.persistence.Table;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
public class InnExcludeDto {

    private Bank bank;
    private String inn;
    private LocalDateTime createDate;

    public static InnExcludeDto of(InnExcludeEntity entity) {
        InnExcludeDto dto = new InnExcludeDto();
        dto.setInn(entity.getId().getInn());
        dto.setBank(entity.getId().getBank());
        dto.setCreateDate(entity.getCreateDate());
        return dto;
    }
}
