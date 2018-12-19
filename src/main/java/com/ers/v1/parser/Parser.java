/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.parser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import com.ers.v1.reader.exceptions.UnableToParseDateException;

/**
 *
 * @author snyanakieva
 * @param <ResultType> - object type to be parsed to
 */
public abstract class Parser<ResultType> {

    private final static Logger LOGGER = Logger.getLogger(Parser.class.getCanonicalName());
    public final static String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";
    protected final DataFormatter dataFormatter = new DataFormatter();
    protected  SimpleDateFormat dateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);

    protected Calendar toCalendar(final Cell cell) throws UnableToParseDateException {
        String stringDate = dataFormatter.formatCellValue(cell);
        Date date = toDate(stringDate);
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(date.getTime());
        return calendar;
    }

    protected Double toNumber(final Cell cell) {
        String stringNumber = dataFormatter.formatCellValue(cell);
        Double number = Double.parseDouble(stringNumber);
        return number;
    }

    protected int toInt(final Cell cell) {
        String stringNumber = dataFormatter.formatCellValue(cell);
        int number = Integer.parseInt(stringNumber);
        return number;
    }

    protected Date toDate(String stringDate) throws UnableToParseDateException {
        try {
            Date date = dateFormat.parse(stringDate);
            return date;
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, "Invalid parsing of: {0}", stringDate);
            throw new UnableToParseDateException();
        }
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public DataFormatter getDataFormatter() {
        return dataFormatter;
    }

    public abstract ResultType parse(Row row);
}
