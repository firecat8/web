/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ers.v1.entities;

import com.eurorisksystems.riskengine.ws.v1_1.vo.market.factor.InstrumentMarketFactorVo;
import java.util.Calendar;
import java.util.Map;

/**
 *
 * @author gdimitrova
 */
public class MarketFactorInfoHolder {

    private final String filename;
    private final InstrumentMarketFactorVo marketFactorVo;
    private final Map<Calendar, Double> quotes;

    public MarketFactorInfoHolder(String filename, InstrumentMarketFactorVo marketFactorVo, Map<Calendar, Double> quotes) {
        this.filename = filename;
        this.marketFactorVo = marketFactorVo;
        this.quotes = quotes;
    }

    public String getFilename() {
        return filename;
    }

    public InstrumentMarketFactorVo getMarketFactorVo() {
        return marketFactorVo;
    }

    public Map<Calendar, Double> getQuotes() {
        return quotes;
    }

}
