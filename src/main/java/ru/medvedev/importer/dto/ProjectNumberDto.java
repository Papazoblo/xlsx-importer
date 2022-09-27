package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.entity.ProjectNumberEntity;
import ru.medvedev.importer.enums.Bank;

import java.time.format.DateTimeFormatter;

@Data
public class ProjectNumberDto {

    private Long id;
    private String date;
    private String number;
    private Bank bank;

    public static ProjectNumberDto of(ProjectNumberEntity entity) {
        ProjectNumberDto dto = new ProjectNumberDto();
        dto.setId(entity.getId());
        dto.setBank(entity.getBank());
        dto.setNumber(entity.getNumber());
        dto.setDate(entity.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        return dto;
    }
}
