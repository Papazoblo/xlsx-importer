package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.XlsxRecordDto;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.EventType;
import ru.medvedev.importer.enums.SkorozvonField;
import ru.medvedev.importer.exception.FileHeaderNotFoundException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static org.hibernate.internal.util.StringHelper.isBlank;
import static ru.medvedev.importer.utils.StringUtils.addPhoneCountryCode;
import static ru.medvedev.importer.utils.StringUtils.replaceSpecialCharacters;

@Service
@RequiredArgsConstructor
public class XlsxParserService {

    private final ApplicationEventPublisher eventPublisher;

    public List<String> readColumnHeaders(FileInfoEntity fileInfo, File file) throws IOException {

        FileInputStream fis = new FileInputStream(file);
        Workbook wb = file.getName().endsWith("xlsx") ? new XSSFWorkbook(fis) : new HSSFWorkbook(fis);
        Sheet sheet = wb.getSheetAt(0);
        List<String> headers = new ArrayList<>();
        for (Row row : sheet) {
            if (row.getRowNum() > 0) {
                break;
            }
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.BLANK) {
                    continue;
                }
                if (cell.getCellType() != CellType.STRING) {
                    eventPublisher.publishEvent(new ImportEvent(this, "В файле отсутствует шапка таблицы",
                            EventType.ERROR, fileInfo.getId()));
                    throw new FileHeaderNotFoundException("В файле отсутствует шапка таблицы", fileInfo.getId());
                }
                String value = cell.getStringCellValue();
                if (isNotBlank(value)) {
                    headers.add(value);
                }
            }
        }
        wb.close();
        fis.close();
        return headers;
    }

    public List<XlsxRecordDto> readColumnBody(FileInfoEntity fileInfo) throws IOException {

        FileInputStream fis = new FileInputStream(new File(fileInfo.getPath()));
        Workbook wb = fileInfo.getName().endsWith("xlsx") ? new XSSFWorkbook(fis) : new HSSFWorkbook(fis);

        Sheet sheet = wb.getSheetAt(0);
        List<XlsxRecordDto> list = new ArrayList<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                continue;
            }
            XlsxRecordDto record = new XlsxRecordDto();
            fileInfo.getFieldLinks().keySet().stream()
                    .filter(key -> !fileInfo.getFieldLinks().get(key).isEmpty())
                    .forEach(item -> addFieldValue(record, row, item, fileInfo.getFieldLinks().get(item)));
            if (isNotBlank(record.getOrgInn())) {
                list.add(record);
            }
        }
        wb.close();
        fis.close();

        if (fileInfo.getEnableWhatsAppLink()) {
            list = list.stream().peek(recordDto ->
                    recordDto.setOrgHost(String.format("https://api.whatsapp.com/send?phone=%s", isBlank(recordDto.getPhone()) ?
                            recordDto.getPhone() : recordDto.getOrgPhone()))).collect(Collectors.toList());
        }

        return list;
    }

    private static void addFieldValue(XlsxRecordDto record, Row row, SkorozvonField field, List<Integer> cells) {
        switch (field) {
            case USR_FIO:
                record.setFio(cellValueToString(row, cells));
                break;
            case USR_PHONE:
                record.setPhone(addPhoneCountryCode(replaceSpecialCharacters(cellValueToString(row, cells))));
                break;
            case USR_EMAIL:
                record.setEmail(cellValueToString(row, cells));
                break;
            case USR_CITY:
                record.setCity(cellValueToString(row, cells));
                break;
            case USR_ADDRESS:
                record.setAddress(cellValueToString(row, cells));
                break;
            case USR_REGION:
                record.setRegion(cellValueToString(row, cells));
                break;
            case USR_POSITION:
                record.setPosition(cellValueToString(row, cells));
                break;
            case USR_DESCRIPTION:
                record.setDescription(cellValueToString(row, cells));
                break;
            case ORG_NAME:
                record.setOrgName(cellValueToString(row, cells));
                break;
            case ORG_PHONE:
                record.setOrgPhone(addPhoneCountryCode(replaceSpecialCharacters(cellValueToString(row, cells))));
                break;
            case ORG_EMAIL:
                record.setOrgEmail(cellValueToString(row, cells));
                break;
            case ORG_HOST:
                record.setOrgHost(cellValueToString(row, cells));
                break;
            case ORG_CITY:
                record.setOrgCity(cellValueToString(row, cells));
                break;
            case ORG_ADDRESS:
                record.setOrgAddress(cellValueToString(row, cells));
                break;
            case ORG_REGION:
                record.setOrgRegion(cellValueToString(row, cells));
                break;
            case ORG_ACTIVITY:
                record.setOrgActivity(cellValueToString(row, cells));
                break;
            case ORG_INN:
                String inn = cellValueToString(row, cells);
                record.setOrgInn(inn.length() == 9 || inn.length() == 11 ? "0" + inn : inn);
                break;
            case ORG_KPP:
                record.setOrgKpp(cellValueToString(row, cells));
                break;
            case USD_DESCRIPTION:
                record.setOrgDescription(cellValueToString(row, cells));
                break;
        }
    }

    private static String cellValueToString(Row row, List<Integer> cells) {
        StringBuilder sb = new StringBuilder();
        for (Integer idx : cells) {
            String value = getValueFromCellByType(row.getCell(idx));
            if (isNotBlank(value)) {
                sb.append(value);
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    private static String getValueFromCellByType(Cell cell) {

        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case NUMERIC:
                return String.valueOf(new BigDecimal(cell.getNumericCellValue()));
            case STRING:
                return cell.getStringCellValue();
            default:
                return cell.getStringCellValue();
        }
    }
}
