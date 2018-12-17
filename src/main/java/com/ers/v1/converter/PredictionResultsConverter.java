/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.converter;

import com.eurorisksystems.riskengine.ws.v1_1.vo.prediction.PredictionResultVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.prediction.SeriesWrapperVo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.ers.v1.utils.ConverterUtils;

/**
 *
 * @author gdimitrova
 */
public class PredictionResultsConverter implements JsonConverter<PredictionResultVo> {

    public final static PredictionResultsConverter INSTANCE = new PredictionResultsConverter();
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    private final Map<Calendar, Double> historicalPrices = new TreeMap<Calendar, Double>();
    private final Map<Calendar, Double> predictions = new TreeMap<Calendar, Double>();
    private final Map<Calendar, Double> volaMinus = new TreeMap<Calendar, Double>();
    private final Map<Calendar, Double> volaPlus = new TreeMap<Calendar, Double>();
    private final Set<Calendar> dates = new TreeSet<Calendar>();

    public String convertToSimpleJson(String text) {
        JsonObject jsonResult = new JsonObject();
        jsonResult.add(text, gson.toJsonTree(text));
        return jsonResult.toString();
    }

    @Override
    public String toString(PredictionResultVo result) {
        JsonObject jsonResult = new JsonObject();
        JsonArray resultsArray = new JsonArray();
        storeFormatedResults(result);
        resultsArray.addAll(convertoJsonArray());
        jsonResult.add("results", resultsArray);
        return jsonResult.toString();
    }

    @Override
    public PredictionResultVo toObject(String json) {
        throw new UnsupportedOperationException();
    }

    private void storeFormatedResults(PredictionResultVo result) {
        dates.clear();
        storeResultsInMap(result.getHistoricalPrices(), historicalPrices);
        storeResultsInMap(result.getPredictions(), predictions);
        storeResultsInMap(result.getVolaMinus(), volaMinus);
        storeResultsInMap(result.getVolaPlus(), volaPlus);
    }

    private void storeResultsInMap(List<SeriesWrapperVo> series, Map<Calendar, Double> map) {
        map.clear();
        for (SeriesWrapperVo wrapperVo : series) {
            map.put(wrapperVo.getDate(), wrapperVo.getValue());
            dates.add(wrapperVo.getDate());
        }
    }

    private JsonArray convertoJsonArray() {
        JsonArray jsonArray = new JsonArray();
        dates.forEach(date -> {
            JsonObject jsonResult = new JsonObject();
            jsonResult.add("date", ConverterUtils.INSTANCE.makeJsonDate(date));
            jsonResult.add("historicalPrices", gson.toJsonTree(historicalPrices.get(date)));
            jsonResult.add("predictions", gson.toJsonTree(predictions.get(date)));
            jsonResult.add("volaMinus", gson.toJsonTree(volaMinus.get(date)));
            jsonResult.add("volaPlus", gson.toJsonTree(volaPlus.get(date)));
            jsonArray.add(jsonResult);
        });
        return jsonArray;
    }

}
