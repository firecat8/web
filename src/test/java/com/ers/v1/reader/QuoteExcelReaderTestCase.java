/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.reader;

import com.ers.v1.entities.MapEntry;
import com.ers.v1.parser.QuoteParser;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Plamen
 */
public class QuoteExcelReaderTestCase {

    private final static int POSITION_OF_FIRST_SHEET = 0;
    private final static String FILE_PATH = "src/main/resources/testQuotes.xls";
    private final static String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";
    
    private File file;
    private Workbook workbook;
    private Map<Calendar, Double> contents;
    private DateFormat dateFormat;
    private List<Calendar> dates;

    private QuoteParser quoteParser;

    @Before
    public void setUp() {
        quoteParser = new QuoteParser();
        file = new File(FILE_PATH);
        dateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
        contents = new HashMap<>();
        dates = new ArrayList<>();
        initWorkbook(file);
    }

    @After
    public void tearDown() {
        file = null;
        workbook = null;
        contents = null;
    }

    @Test
    public void isSheetValidTest() {

        Sheet sheet = createSheet();
        sheet.forEach(row -> isRowValid(row.getPhysicalNumberOfCells()));
    }

    @Test
    public void convertDateTest() {

        Sheet sheet = createSheet();
        Cell cell = sheet.getRow(0).getCell(0);
        String dateString = new DataFormatter().formatCellValue(cell);
        try {
            dateFormat.parse(dateString);
        } catch (ParseException ex) {
            fail("Could not parse: " + dateString);
        }
    }

    @Test
    public void isDataSaved() {

        Sheet sheet = createSheet();
        sheet.forEach(row -> parseAndSave(row));
//        contents.keySet().stream().anyMatch(cal -> dates.get(dates.size() / 2).equals(cal));
        boolean actual = contents.keySet().stream().allMatch(cal -> dates.contains(cal));
        assertTrue(actual);
    }

    private void parseAndSave(Row row) {

        MapEntry<Calendar, Double> quotePair = quoteParser.parse(row);
        dates.add(quotePair.getKey());
        contents.put(quotePair.getKey(), quotePair.getValue());
    }

    private void isRowValid(int rowCellCount) {
        if (rowCellCount != 2) {
            Assert.fail("Only 2 cells allowed per row!");
        }
    }

    private void initWorkbook(final File file) {
        try {
            workbook = WorkbookFactory.create(file);
        } catch (IOException | InvalidFormatException ex) {
            Logger.getLogger(QuoteExcelReaderTestCase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Sheet createSheet() {
        return workbook.getSheetAt(POSITION_OF_FIRST_SHEET);
    }

}
