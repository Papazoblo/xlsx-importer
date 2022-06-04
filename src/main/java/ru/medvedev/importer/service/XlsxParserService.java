package ru.medvedev.importer.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Service
public class XlsxParserService {

    public List<String> readColumnHeaders() throws IOException {

        FileInputStream fis = new FileInputStream(new File("D:\\example.xlsx"));
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
        return headers;
    }
}
