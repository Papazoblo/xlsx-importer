package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.entity.FileInfoBankEntity;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.FileInfoBankStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Data
public class FileInfoBankDto {

    private Bank bank;
    private String projectId;
    private FileInfoBankStatus status;

    public static FileInfoBankDto of(FileInfoBankEntity entity) {
        FileInfoBankDto dto = new FileInfoBankDto();
        dto.setBank(entity.getBank());
        dto.setStatus(entity.getDownloadStatus());
        dto.setProjectId(String.valueOf(entity.getProjectId()));
        return dto;
    }
}
