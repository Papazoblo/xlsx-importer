package ru.medvedev.importer.service.export.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.ColumnInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class XlsExporter<T> extends BaseExporterImpl<T> {

    private static final Integer MAX_RECORDS = 1_000_000;
    private static final int WINDOW_SIZE = 100;

    public InputStreamResource export() throws IOException {

        List<ColumnInfo<T>> columns = getColumnList();

        SXSSFWorkbook workbook = new SXSSFWorkbook(WINDOW_SIZE);

        Font fontHeader = workbook.createFont();
        fontHeader.setFontName("Arial");
        fontHeader.setFontHeightInPoints((short) 12);
        fontHeader.setBold(true);

        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);


        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setWrapText(true);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFont(fontHeader);
        headerStyle.setBorderBottom(BorderStyle.THICK);
        headerStyle.setBorderLeft(BorderStyle.MEDIUM);
        headerStyle.setBorderRight(BorderStyle.MEDIUM);

        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);


        if (data.isEmpty()) {
            initSheet(workbook, 1, columns, headerStyle);
        } else {
            for (int rowStartNum = 0; rowStartNum <= data.size(); rowStartNum += MAX_RECORDS) {

                SXSSFSheet sheet = initSheet(workbook, rowStartNum / MAX_RECORDS + 1, columns, headerStyle);

                int i = 2;
                for (T record : new ArrayList<>(data).subList(rowStartNum, Math.min(data.size(), rowStartNum + MAX_RECORDS))) {
                    Row row = sheet.createRow(i);

                    for (int j = 0; j < columns.size(); j++) {
                        Cell cell = row.createCell(j);
                        cell.setCellValue(columns.get(j).getInfoGetter().apply(record));
                        cell.setCellStyle(style);
                    }
                    i++;

                    if (i % WINDOW_SIZE == 0) {
                        sheet.flushRows(WINDOW_SIZE);
                    }
                }
            }
        }


        byte[] bytes;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.dispose();
        workbook.close();
        bytes = bos.toByteArray();

        return new InputStreamResource(new ByteArrayInputStream(bytes));
    }

    private SXSSFSheet initSheet(SXSSFWorkbook workbook, int rowStartNum, List<ColumnInfo<T>> columns, CellStyle headerStyle) {
        SXSSFSheet sheet = workbook.createSheet(String.valueOf(rowStartNum / MAX_RECORDS + 1));
        for (int i = 0; i < columns.size(); i++) {
            sheet.setColumnWidth(i, columns.get(i).getWidth() * 100);
        }


        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columns.size()));
        Row title = sheet.createRow(0);
        Cell titleCell = title.createCell(0);
        titleCell.setCellValue(getFullTitle());
        titleCell.setCellStyle(headerStyle);

        Row header = sheet.createRow(1);
        for (int i = 0; i < columns.size(); i++) {
            Cell headerCell = header.createCell(i);
            headerCell.setCellValue(columns.get(i).getName());
            headerCell.setCellStyle(headerStyle);
        }
        return sheet;
    }
}
