package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.ColumnInfoDto;
import ru.medvedev.importer.dto.FieldNameVariantDto;
import ru.medvedev.importer.dto.FieldPositionDto;
import ru.medvedev.importer.dto.HeaderDto;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.EventType;
import ru.medvedev.importer.enums.XlsxRequireField;
import ru.medvedev.importer.exception.FileHeaderNotFoundException;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static ru.medvedev.importer.enums.XlsxRequireField.*;

@Service
@RequiredArgsConstructor
public class HeaderProcessingService {

    private final ApplicationEventPublisher eventPublisher;
    private final FileInfoService fileInfoService;

    public ColumnInfoDto headerProcessing(FileInfoEntity fileInfo, Map<XlsxRequireField, FieldNameVariantDto> namesMap,
                                          FileInputStream fis) throws IOException {

        Workbook wb = fileInfo.getName().endsWith("xlsx") ? new XSSFWorkbook(fis) : new HSSFWorkbook(fis);

        Sheet sheet = wb.getSheetAt(0);
        Map<XlsxRequireField, FieldPositionDto> fieldPositionMap;
        try {
            fieldPositionMap = parseHeader(sheet.getRow(0), namesMap, fileInfo.getId());
            fileInfo.setWithHeader(true);
        } catch (FileHeaderNotFoundException ex) {
            fieldPositionMap = new HashMap<>();
            fileInfo.setWithHeader(false);
        }
        fileInfoService.save(fileInfo);
        Map<Integer, List<String>> columnInfoMap = readLines(sheet);
        ColumnInfoDto columnInfo = new ColumnInfoDto();
        columnInfo.setColumnInfoMap(columnInfoMap);
        columnInfo.setFieldPositionMap(fieldPositionMap);
        wb.close();
        return columnInfo;
    }


    private Map<XlsxRequireField, FieldPositionDto> parseHeader(Row row,
                                                                Map<XlsxRequireField, FieldNameVariantDto> namesMap,
                                                                Long fileId) {
        Map<XlsxRequireField, FieldPositionDto> positionField = new HashMap<>();

        List<XlsxRequireField> emptyFieldsList = new ArrayList<>();
        namesMap.keySet().forEach(key -> {
            if (key == XlsxRequireField.TRASH) {
                return;
            }
            FieldPositionDto dto = new FieldPositionDto();
            FieldNameVariantDto fieldNameVariantDto = namesMap.get(key);
            dto.setRequired(fieldNameVariantDto.isRequired());

            for (Cell cell : row) {
                if (cell.getCellType() == CellType.BLANK) {
                    continue;
                }
                if (cell.getCellType() != CellType.STRING) {
                    eventPublisher.publishEvent(new ImportEvent(this, "В файле отсутствует шапка таблицы",
                            EventType.LOG_TG, fileId));
                    throw new FileHeaderNotFoundException("В файле отсутствует шапка таблицы", fileId);
                }

                if (fieldNameVariantDto.getNames().stream()
                        .anyMatch(name -> name.toLowerCase().equals(cell.getStringCellValue().toLowerCase()))) {
                    HeaderDto header = new HeaderDto();
                    header.setPosition(cell.getColumnIndex());
                    header.setValue(cell.getStringCellValue());
                    dto.getHeader().add(header);
                    break;
                }
            }

            if (dto.isRequired() && dto.getHeader().isEmpty()) {
                emptyFieldsList.add(key);
            }
            positionField.put(key, dto);
        });

        if (!emptyFieldsList.isEmpty()) {
            boolean fioIsExists = emptyFieldsList.stream()
                    .anyMatch(field -> field == XlsxRequireField.FIO);
            String fields = emptyFieldsList.stream()
                    .filter(field -> (!fioIsExists && (Stream.of(NAME, SURNAME, MIDDLE_NAME).noneMatch(item -> item == field))) ||
                            (fioIsExists && Stream.of(FIO).noneMatch(item -> item == field)))
                    .map(XlsxRequireField::getDescription)
                    .collect(Collectors.joining(", "));
            if (isNotBlank(fields.trim())) {
                eventPublisher.publishEvent(new ImportEvent(this, String.format("Столбец %s не найден в файле",
                        fields), EventType.LOG_TG, fileId));
            }
        }

        //positionField.put(XlsxRequireField.TRASH, parseTrashColumns(row, positionField));
        return positionField;
    }


    private Map<Integer, List<String>> readLines(Sheet sheet) {

        Row firstRow = sheet.getRow(0);
        Map<Integer, List<String>> columnInfos = new HashMap<>();
        for (int i = firstRow.getFirstCellNum(); i <= firstRow.getLastCellNum() - 1; i++) {
            List<String> lines = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                lines.add(getCellValue(sheet.getRow(j).getCell(i)));
            }
            columnInfos.put(i, lines);
        }
        return columnInfos;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return Optional.ofNullable(cell.getStringCellValue()).orElse("");
            case NUMERIC:
                return Optional.of(cell.getNumericCellValue())
                        .map(val -> new BigDecimal(val).toString()).orElse("");
            default:
                return "";
        }
    }
}
