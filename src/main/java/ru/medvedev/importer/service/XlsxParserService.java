package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.config.XlsxStorage;
import ru.medvedev.importer.exception.BadRequestException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Service
@RequiredArgsConstructor
public class XlsxParserService {

    private final XlsxStorage storage;

    public List<String> readColumnHeaders() throws IOException {

        if (!storage.isExist()) {
            throw new BadRequestException("Необходимо загрузить файл");
        }

        FileInputStream fis = new FileInputStream(new File(storage.getFileName()));
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet = wb.getSheetAt(0);
        List<String> headers = new ArrayList<>();
        for (Row row : sheet) {
            if (row.getRowNum() > 0) {
                break;
            }
            for (Cell cell : row) {
                String value = cell.getStringCellValue();
                if (isNotBlank(value)) {
                    headers.add(value);
                }
            }
        }
        Collections.sort(headers);
        wb.close();
        fis.close();
        return headers;
    }
}
