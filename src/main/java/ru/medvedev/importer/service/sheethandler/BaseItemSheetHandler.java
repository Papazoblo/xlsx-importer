package ru.medvedev.importer.service.sheethandler;


import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import ru.medvedev.importer.exception.ExcelParsingException;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static feign.Util.isBlank;

@Slf4j
public abstract class BaseItemSheetHandler extends DefaultHandler {

    protected static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final Pattern pattern = Pattern.compile("([A-Z]+)(\\d+)");

    private final SharedStringsTable sst;

    protected Map<String, String> rowMap;

    private boolean nextIsString;
    private boolean skipRow = false;
    private String currentColumn;
    private String lastContents;
    protected int preventRow;

    public BaseItemSheetHandler(SharedStringsTable sst) {
        this.sst = sst;
    }

    public void startElement(String uri, String localName, String name,
                             Attributes attributes) throws SAXException {

        if (name.equals("c")) {
            String cellName = attributes.getValue("r");
            Matcher matcher = pattern.matcher(cellName);
            int currentRow;
            if (matcher.find()) {
                currentColumn = matcher.group(1);
                currentRow = Integer.parseInt(matcher.group(2));
                if (currentRow == getStartIndexRowData() && rowMap == null) {
                    rowMap = new HashMap<>();
                    preventRow = currentRow;
                } else if (currentRow > getStartIndexRowData() && preventRow != currentRow) {
                    if (!skipRow) {
                        createItem();
                    }
                    rowMap.clear();
                    skipRow = false;
                    preventRow = currentRow;
                }
            }
            String cellType = attributes.getValue("t");
            if (cellType != null && cellType.equals("s")) {
                nextIsString = true;
            } else {
                nextIsString = false;
            }
        }
        lastContents = "";
    }

    @Override
    public void endDocument() {
        log.debug("Документ прочитан");
        if (!skipRow) {
            createItem();
        }
        rowMap.clear();
    }

    public void endElement(String uri, String localName, String name) {
        if (preventRow < getStartIndexRowData()) {
            return;
        }
        if (nextIsString) {
            int idx = Integer.parseInt(lastContents);
            lastContents = sst.getItemAt(idx).getString();
            nextIsString = false;
        }
        if (currentColumn.equals("A") && isBlank(lastContents)) {
            skipRow = true;
            return;
        }
        if (name.equals("v")) {
            rowMap.put(currentColumn, lastContents);
        }
    }

    public void characters(char[] ch, int start, int length) {
        lastContents += new String(ch, start, length);
    }

    protected void setValue(String columnName, Consumer<String> setter) {
        setValue(columnName, setter, false);
    }

    protected void setValue(String columnName, Consumer<String> setter, boolean allowBeEmpty) {
        String cellValue = rowMap.get(columnName);
        if (cellValue != null) {
            if (!allowBeEmpty && !cellValue.isEmpty()) {
                setter.accept(cellValue);
            } else if (allowBeEmpty) {
                setter.accept(cellValue);
            } else {
                log.trace(String.format("*** value is blank in cell[%s%d] ", columnName, preventRow));
            }
        }
    }

    protected boolean setValue(String columnName, Consumer<String> setter, StringBuilder sb, int limit) {
        boolean failed = false;
        String cellValue = rowMap.get(columnName);
        if (checkValueLengthLimit(cellValue, limit)) {
            try {
                setter.accept(cellValue);
            } catch (Throwable t) {
                sb.append("Ошибка обработки поля, ").append("[").append(columnName).append(preventRow).append("]\n");
                failed = true;
            }

        } else {
            sb.append("Превышена максимальная длина поля, ").append(limit).append(" символов, ячейка ").append("[").append(columnName).append(preventRow).append("]\n");
            failed = true;
        }
        return failed;
    }
    protected boolean setValue(String columnName, Consumer<String> setter, StringBuilder sb, String parseExceptionUserComment) {
        return setValue(columnName, setter, sb, true, -1, null, parseExceptionUserComment);
    }
    protected boolean setValue(String columnName, Consumer<String> setter, StringBuilder sb, boolean emptyValueAllowed, int limit, String emptyValueUserComment) {
        return setValue(columnName, setter, sb, emptyValueAllowed, limit, emptyValueUserComment, null);
    }

    protected boolean setValue(String columnName, Consumer<String> setter, StringBuilder sb, boolean emptyValueAllowed, String emptyValueUserComment, String parseExceptionUserComment) {
        return setValue(columnName, setter, sb, emptyValueAllowed, -1, emptyValueUserComment, parseExceptionUserComment);
    }
    protected boolean setValue(String columnName, Consumer<String> setter, StringBuilder sb, boolean emptyValueAllowed, int limit, String emptyValueUserComment, String parseExceptionUserComment) {
        String cellValue = rowMap.get(columnName);
        boolean failed = false;
        try {
            if (!checkValueLengthLimit(cellValue, limit)) {
                sb.append("Превышена максимальная длина поля, ").append(limit).append(" символов, ячейка ").append("[").append(columnName).append(preventRow).append("]\n");
                failed = true;
            } else {
                if (emptyValueAllowed) {
                    failed = safeAccept(columnName, setter, cellValue, sb, parseExceptionUserComment);
                } else {
                    if (isBlank(cellValue)) {
                        sb.append(emptyValueUserComment).append("[").append(columnName).append(preventRow).append("]\n");
                        failed = true;
                    } else {
                        failed = safeAccept(columnName, setter, cellValue, sb, parseExceptionUserComment);
                    }
                }
            }

        } catch (Throwable t) {
            log.error("General exception, cell[{}{}]", columnName,preventRow, t);
            sb.append(t.getMessage()).append("\n");
            failed = true;
        }
        return failed;
    }
    private boolean safeAccept(String columnName, Consumer<String> setter, String cellValue, StringBuilder sb, String parseExceptionUserComment) {
        try {
            setter.accept(cellValue);
        } catch (ExcelParsingException e) {
            sb.append(parseExceptionUserComment).append(" [").append(columnName).append(preventRow).append("]\n");
            return true;
        } catch (Throwable t) {
            //log.error("Exception during cell parsing, cell[{}{}]", columnName, preventRow, t); //too many spam
            log.debug("Exception during cell parsing, cell[{}{}], value: {}, comment: {}, message: {}", columnName, preventRow, cellValue, parseExceptionUserComment, t.getMessage());
            sb.append(parseExceptionUserComment).append(" [").append(columnName).append(preventRow).append("]\n");
            return true;
        }
        return false;
    }
    protected abstract int getStartIndexRowData();

    protected abstract void createItem();

    private boolean checkValueLengthLimit(String value, int limit) {
        if (limit < 0) {
            return true;
        }
        if (isBlank(value)) {
            return true;
        }

        return value.length() <= limit;
    }
}
