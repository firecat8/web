/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.converter;

import com.ers.v1.utils.ConverterUtils;
import com.eurorisksystems.riskengine.ws.v1_1.vo.FrequencyVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.prediction.PredictionConfigVo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * @author gdimitrova
 */
public class PredictionConfigVoConverter implements JsonConverter<PredictionConfigVo> {
    
	private final JsonParser JSON_PARSER = new JsonParser();
	public final static PredictionConfigVoConverter INSTANCE = new PredictionConfigVoConverter();

	@Override
	public String toString(PredictionConfigVo obj) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public PredictionConfigVo toObject(String json) {
		JsonObject asJsonObject = JSON_PARSER.parse(json).getAsJsonObject();
		return new PredictionConfigVo(
				ConverterUtils.INSTANCE.makeTenorVo(asJsonObject.getAsJsonObject("historicalInterval")),
				ConverterUtils.INSTANCE.makeTenorVo(asJsonObject.getAsJsonObject("backwardAnalyticalPeriod")),
				FrequencyVo.valueOf(asJsonObject.get("historicalPriceFrequency").getAsString()),
				ConverterUtils.INSTANCE.makeTenorVo(asJsonObject.getAsJsonObject("predictionHorizon")),
				asJsonObject.get("historicalPredictionStep").getAsInt());
	}

}
