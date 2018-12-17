/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.parser;

import com.ers.v1.entities.MapEntry;
import java.util.Calendar;
import org.apache.poi.ss.usermodel.Row;

/**
 *
 * @author Plamen
 */
public class QuoteParser extends Parser<MapEntry<Calendar, Double>> {

    @Override
    public MapEntry<Calendar, Double> parse(Row row) {
        return parse(row, 0, 1);
    }

    public MapEntry<Calendar, Double> parse(Row row, int dateColumnIndex, int valueColumnIndex) {
        Calendar calendar = toCalendar(row.getCell(dateColumnIndex));
        Double number = toNumber(row.getCell(valueColumnIndex));
        MapEntry<Calendar, Double> quotePair = new MapEntry<>(calendar, number);
        return quotePair;
    }
}
