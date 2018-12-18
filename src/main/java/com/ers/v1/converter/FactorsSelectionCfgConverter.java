/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ers.v1.converter;

import com.ers.v1.servlet.esg.FactorsSelectionConfig;
import com.ers.v1.utils.ConverterUtils;
import com.eurorisksystems.riskengine.ws.v1_1.vo.FrequencyVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.FactorSelectorVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.portfolio.evaluation.EvaluationIdVo;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * @author gdimitrova
 */
public class FactorsSelectionCfgConverter implements JsonConverter<FactorsSelectionConfig> {

	private final JsonParser JSON_PARSER = new JsonParser();
	public final static FactorsSelectionCfgConverter INSTANCE = new FactorsSelectionCfgConverter();

	@Override
	public String toString(FactorsSelectionConfig obj) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public FactorsSelectionConfig toObject(String json) {
		JsonObject asJsonObject = JSON_PARSER.parse(json).getAsJsonObject();
		return new FactorsSelectionConfig(
				asJsonObject.get("seriesId").getAsString(),
				asJsonObject.get("seriesName").getAsString(),
				ConverterUtils.INSTANCE.makeTenorVo(asJsonObject.getAsJsonObject("historicalInterval")),
				FrequencyVo.valueOf(asJsonObject.get("frequency").getAsString()),
				FactorSelectorVo.valueOf(asJsonObject.get("suggestionMethod").getAsString()),
				asJsonObject.get("maxSuggestions").getAsInt(),
				asJsonObject.get("minimalQuality").getAsDouble(),
				new EvaluationIdVo(asJsonObject.get("evaluationId").getAsString())
		);
	}

}
