package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.entity.FileInfoBankEntity;
import ru.medvedev.importer.entity.FileInfoEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class FileInfoDto {

    private Long id;
    private String name;
    private String size;
    private String date;
    private String status;
    private String source;
    private boolean deleted;
    private List<FileInfoBankDto> fileInfoBankList = new ArrayList<>();

    public static FileInfoDto of(FileInfoEntity entity) {
        FileInfoDto dto = new FileInfoDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setStatus(entity.getStatus().getDescription());
        dto.setSize(new BigDecimal(String.valueOf(entity.getSize())).divide(new BigDecimal(1048576), 2, RoundingMode.FLOOR).toString() + " mb");
        dto.setDate(entity.getCreateAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        dto.setSource(entity.getSource().getDescription());
        dto.setDeleted(entity.isDeleted());
        dto.setFileInfoBankList(entity.getBankList().stream()
                .map(FileInfoBankDto::of)
                .collect(Collectors.toList()));
        return dto;
    }
}
