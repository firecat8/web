package com.ers.v1.parser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.junit.After;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 *???????????????????
 * @author Plamen
 */
public class QuoteParserTestCase {

    private final static Logger LOGGER = Logger.getLogger(QuoteParser.class.getCanonicalName());
    private final static String SIMPLE_DATE_FORMAT = "dd/MM/yyyy";

    private DataFormatter dataFormatter;
    private DateFormat dateFormat;

    @Before
    public void setUp() {

        dataFormatter = new DataFormatter();
        dateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);

    }

    @After
    public void tearDown() {
        dataFormatter = null;
        dateFormat = null;
    }

    @Test
    public void parseToDateTest() {
        String dateString = "12/12/2001";
        Date date = parseToDate(dateString);

    }

    private Date parseToDate(String dateString) {
        Date date = null;
        try {

            date = dateFormat.parse(dateString);
        } catch (ParseException ex) {
            fail("Could not parse string to date. Passed value: " + dateString);
        } finally {
            return date;
        }
    }

}
