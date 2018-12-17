/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.converter;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.ers.v1.utils.ConverterUtils;
import com.eurorisksystems.riskengine.ws.v1_1.vo.descriptions.MarketFactorQuoteDescriptionVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.market.factor.quote.MarketFactorQuoteVo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 *
 * @author snyanakieva
 */
public class MarketFactorQuotesVoConverter implements JsonConverter<List<MarketFactorQuoteDescriptionVo>> {

    public static MarketFactorQuotesVoConverter INSTANCE = new MarketFactorQuotesVoConverter();

    @Override
    public String toString(List<MarketFactorQuoteDescriptionVo> obj) {
        return convertToJsonArray(obj, "quotes").toString();
    }

    @Override
    public List<MarketFactorQuoteDescriptionVo> toObject(String json) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public JsonObject convertToJsonArray(List<MarketFactorQuoteDescriptionVo> quotes, String propName) {
        JsonObject quotesObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        quotes.forEach(quote -> {
            JsonObject jsonResult = new JsonObject();
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(quote.getEntityId().getAtTime());
            jsonResult.add("date", ConverterUtils.INSTANCE.makeJsonDate(calendar));
            jsonResult.addProperty("value", quote.getValue());
            jsonArray.add(jsonResult);
        });
        quotesObject.add(propName, jsonArray);
        return quotesObject;
    }

    public JsonArray convertQuotesToJsonArray(List<MarketFactorQuoteVo> marketFactorQuotes) {
        JsonArray quotesArray = new JsonArray();
        marketFactorQuotes.forEach(marketFactorQuote->{
            JsonObject jsonResult = new JsonObject();
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(marketFactorQuote.getValidFrom());
            jsonResult.add("date", ConverterUtils.INSTANCE.makeJsonDate(calendar));
            jsonResult.addProperty("value", marketFactorQuote.getValue());
            quotesArray.add(jsonResult);
        }
        );
        return quotesArray;
    }

  
}
