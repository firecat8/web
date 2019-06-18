/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.reader;

import com.ers.v1.entities.MapEntry;
import com.ers.v1.parser.Parser;
import com.ers.v1.reader.exceptions.UnableToParseDateException;
import com.ers.v1.reader.exceptions.InvalidSheetFormatException;
import com.ers.v1.parser.QuoteParser;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.format.CellFormatType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 *
 * @author Plamen
 */
public class QuoteExcelReader extends ExcelReader {

    private final static int VALID_ROW_CELL_COUNT = 2;

    private Map<Calendar, Double> contents;
    private QuoteParser quoteParser;
    private boolean hasHeaders;
    private int dateColumnIndex;
    private int valueColumnIndex;

    public QuoteExcelReader() {
        contents = new HashMap<>();
        quoteParser = new QuoteParser();
        hasHeaders = false;
        dateColumnIndex = -1;
        valueColumnIndex = -1;
    }

    @Override
    public void readFromStream(InputStream inputStream)
            throws InvalidSheetFormatException, UnableToParseDateException {
        contents.clear();
        super.readFromStream(inputStream);
    }

    public Map<Calendar, Double> getContents() {
        return contents;
    }

    @Override
    protected void checkSheetValidity(final Sheet sheet) {
        Row firstRow = sheet.getRow(0);
        if (firstRow == null || firstRow.getPhysicalNumberOfCells() == 0) {
            throw new InvalidSheetFormatException();
        }
        hasHeaders = checkForHeaders(firstRow);
        if (!hasHeaders) {
            dateColumnIndex = 0;
            valueColumnIndex = 1;
        }
        sheet.forEach((Row row) -> {
            if (row.getPhysicalNumberOfCells() < VALID_ROW_CELL_COUNT) {
                throw new InvalidSheetFormatException();
            }
            checkDateFormat(row, row.getRowNum());
            checkValueFormat(row, row.getRowNum());
        });
    }

    @Override
    protected void parseAndSave(Row row) {
        if (row.getRowNum() == 0 && hasHeaders) {
            return;
        }
        MapEntry<Calendar, Double> quotePair = quoteParser.parse(row, dateColumnIndex, valueColumnIndex);
        // RiskEngine don't save quote with value zero value
        if (quotePair.getValue() > 0) {
            contents.put(quotePair.getKey(), quotePair.getValue());
        }
    }

    private boolean checkForHeaders(Row row) {
        DataFormatter df = quoteParser.getDataFormatter();
        for (Cell cell : row) {
            String val = df.formatCellValue(cell);
            switch (val.toLowerCase()) {
                case "date":
                    dateColumnIndex = cell.getColumnIndex();
                    break;
                case "close":
                    valueColumnIndex = cell.getColumnIndex();
                    break;
                default:
                    if (dateColumnIndex != -1 && valueColumnIndex != -1) {
                        return true;
                    }
            }
        }
        return false;
    }

    private void checkDateFormat(Row row, int rowNum) {
        if (rowNum == 0 && hasHeaders) {
            return;
        }
        Cell cell = row.getCell(dateColumnIndex);
        if (!DateUtil.isADateFormat(cell.getCellStyle().getDataFormat(), cell.getCellStyle().getDataFormatString())) {
            throwException("Not valid date format", rowNum, cell);
        }
        try {
            double value = cell.getNumericCellValue();
            if (!DateUtil.isValidExcelDate(value)) {
                throwException("Not valid excel date format", rowNum, cell);
            }

        } catch (Exception ex) {
            throwException("Not valid excel date format", rowNum, cell);
        }
    }

    private void throwException(String msg, int rowNum, Cell cell) {
        throw new IllegalArgumentException(msg + " on row "
                + (rowNum + 1) + " and column " + (dateColumnIndex + 1)
                + ". Format " + cell.getCellStyle().getDataFormatString()
                + ". Value " + quoteParser.getDataFormatter().formatCellValue(cell)
        );

    }

    private void checkValueFormat(Row row, int rowNum) {
        if (rowNum == 0 && hasHeaders) {
            return;
        }
        Cell cell = row.getCell(valueColumnIndex);
        if (cell.getCellType() != CellFormatType.GENERAL.ordinal()
                && cell.getCellType() != CellFormatType.NUMBER.ordinal()) {
            throwException("Not valid value format", rowNum, cell);
        }
        try {
            double value = cell.getNumericCellValue();

        } catch (Exception ex) {
            throwException("Not valid value format", rowNum, cell);
        }
    }
}
