package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.entity.FileInfoEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Data
public class FileInfoDto {

    private Long id;
    private String name;
    private String size;
    private String date;
    private String status;
    private boolean deleted;

    public static FileInfoDto of(FileInfoEntity entity) {
        FileInfoDto dto = new FileInfoDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setStatus(entity.getStatus().getDescription());
        dto.setSize(new BigDecimal(entity.getSize()).divide(new BigDecimal(1048576), 2, RoundingMode.FLOOR).toString() + " mb");
        dto.setDate(entity.getCreateAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        dto.setDeleted(entity.isDeleted());
        return dto;
    }
}
