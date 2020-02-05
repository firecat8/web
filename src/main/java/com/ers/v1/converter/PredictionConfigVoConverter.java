/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.converter;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.ers.v1.utils.ConverterUtils;
import com.eurorisksystems.riskengine.ws.v1_1.vo.FrequencyVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.TenorVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.prediction.PredictionConfigVo;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * @author gdimitrova
 */
public class PredictionConfigVoConverter implements JsonConverter<PredictionConfigVo> {

	private final JsonParser JSON_PARSER = new JsonParser();
	public final static PredictionConfigVoConverter INSTANCE = new PredictionConfigVoConverter();
	private final static Map<FrequencyVo, TenorVo> OPTIMIZATION_MAP = new EnumMap<>(FrequencyVo.class);

	static {
		OPTIMIZATION_MAP.put(FrequencyVo.DAILY, new TenorVo(2, 0, 0));
		OPTIMIZATION_MAP.put(FrequencyVo.WEEKLY, new TenorVo(10, 0, 0));
		OPTIMIZATION_MAP.put(FrequencyVo.TWO_WEEKS, new TenorVo(20, 0, 0));
		OPTIMIZATION_MAP.put(FrequencyVo.MONTHLY, new TenorVo(40, 0, 0));
		OPTIMIZATION_MAP.put(FrequencyVo.TWO_MONTHS, new TenorVo(80, 0, 0));
	}

	@Override
	public String toString(PredictionConfigVo obj) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public PredictionConfigVo toObject(String json) {
		JsonObject asJsonObject = JSON_PARSER.parse(json).getAsJsonObject();
		Boolean optimizeSeries = asJsonObject.get("optimizeSeries").getAsBoolean();
		TenorVo historicalInterval = ConverterUtils.INSTANCE.makeTenorVo(asJsonObject.getAsJsonObject("historicalInterval"));
		FrequencyVo historicalPriceFrequency = FrequencyVo.valueOf(asJsonObject.get("historicalPriceFrequency").getAsString());
		if (optimizeSeries) {
			TenorVo optimizedHistoricalInterval = OPTIMIZATION_MAP.get(historicalPriceFrequency);
			if (optimizedHistoricalInterval != null && historicalInterval.getYears() > optimizedHistoricalInterval.getYears()) {
				historicalInterval = optimizedHistoricalInterval;
			}
		}
		return new PredictionConfigVo(
				historicalInterval,
				ConverterUtils.INSTANCE.makeTenorVo(asJsonObject.getAsJsonObject("backwardAnalyticalPeriod")),
				historicalPriceFrequency,
				ConverterUtils.INSTANCE.makeTenorVo(asJsonObject.getAsJsonObject("predictionHorizon")),
				asJsonObject.get("historicalPredictionStep").getAsInt());
	}

}
