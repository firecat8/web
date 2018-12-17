/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ers.v1.convertor;

import com.ers.v1.converter.PredictionResultsConverter;
import com.eurorisksystems.riskengine.ws.v1_1.vo.prediction.PredictionResultVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.prediction.SeriesWrapperVo;

import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gdimitrova
 */
public class PredictionResltsJsonConvertorTest {

    private final Calendar MARKET_DATE = new GregorianCalendar(2005, 3, 1);

    @Test
    public void testConvertoJson() {
        PredictionResultVo createPredictionResultVo = createPredictionResultVo();
        String convertoJson = PredictionResultsConverter.INSTANCE.toString(createPredictionResultVo);
        assertEquals(" Json format is not match! "+convertoJson+"\n",
                "{\"results\":[{\"date\":\"1/4/2005\",\"historicalPrices\":200.0,\"predictions\":200.0,\"volaMinus\":200.0,\"volaPlus\":200.0}]}",
                convertoJson);
        /* 
        {"results":[{"date":"1/4/2005","value":100.0,"resultName":"historicalPrices"},{"date":"1/4/2005","value":200.0,"resultName":"historicalPrices"},{"date":"1/4/2005","value":100.0,"resultName":"predictions"},{"date":"1/4/2005","value":200.0,"resultName":"predictions"},{"date":"1/4/2005","value":100.0,"resultName":"volaMinus"},{"date":"1/4/2005","value":200.0,"resultName":"volaMinus"},{"date":"1/4/2005","value":100.0,"resultName":"volaPlus"},{"date":"1/4/2005","value":200.0,"resultName":"volaPlus"}]}
        */
    }

    private PredictionResultVo createPredictionResultVo() {
        PredictionResultVo result = new PredictionResultVo();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(MARKET_DATE.getTime());
        List<SeriesWrapperVo> series = new ArrayList<>();
        for (int i = 1; i < 3; i++) {
            series.add(new SeriesWrapperVo(calendar, i * 100.));
        }
        result.setHistoricalPrices(series);
        result.setPredictions(series);
        result.setVolaMinus(series);
        result.setVolaPlus(series);
        return result;
    }

}
