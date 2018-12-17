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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javafx.util.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
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
        Row r = sheet.getRow(0);
        hasHeaders = checkForHeaders(r);
        if (!hasHeaders) {
            dateColumnIndex = 0;
            valueColumnIndex = 1;
        } else {
            checkDateFormat(sheet.getRow(1));
        }
        sheet.forEach((Row row) -> {
            if (row.getPhysicalNumberOfCells() < VALID_ROW_CELL_COUNT) {
                throw new InvalidSheetFormatException();
            }
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

    private void checkDateFormat(Row row) {
        Cell cell = row.getCell(dateColumnIndex);
        String format = cell.getCellStyle().getDataFormatString();
        if (!format.equals(Parser.SIMPLE_DATE_FORMAT)) {
            quoteParser.applyDateFormat(format);
        }
    }

}
