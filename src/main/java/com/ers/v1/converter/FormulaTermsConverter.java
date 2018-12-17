/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.converter;

import java.util.ArrayList;
import java.util.List;

import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.multifactor.FormulaTermVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.multifactor.PowerFunctionVo;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * @author snyanakieva
 */
public class FormulaTermsConverter implements JsonConverter<List<FormulaTermVo>> {

	public static FormulaTermsConverter INSTANCE = new FormulaTermsConverter();
	private final JsonParser JSON_PARSER = new JsonParser();

	@Override
	public String toString(List<FormulaTermVo> obj) {
		return convertoJson(obj).toString();
	}

	@Override
	public List<FormulaTermVo> toObject(String json) {
		JsonObject asJsonObject = JSON_PARSER.parse(json).getAsJsonObject();
		List<FormulaTermVo> terms = new ArrayList<>();
		JsonArray jsonList = asJsonObject.get("mfIds").getAsJsonArray();
		for (JsonElement element : jsonList) {
			FormulaTermVo term = new FormulaTermVo();
			term.setCoefficient(0.0);
			term.setFunction(new PowerFunctionVo());
			term.setMarketFactorId(element.getAsString());
			terms.add(term);
		}
		return terms;
	}

	public JsonObject convertoJson(List<FormulaTermVo> terms) {
		JsonObject seriesObject = new JsonObject();
		JsonArray array = new JsonArray();
		for (FormulaTermVo term : terms) {
			JsonObject quotesObject = new JsonObject();
			quotesObject.addProperty("termCoeficient", term.getCoefficient());
			String function = term.getFunction() != null
					? term.getFunction().getClass().getSimpleName()
					: null;
			quotesObject.addProperty("function", function);
			Double functionArgument = term.getFunction() != null
					&& term.getFunction() instanceof PowerFunctionVo
					? ((PowerFunctionVo) term.getFunction()).getPower()
					: null;
			quotesObject.addProperty("argument", functionArgument);
			quotesObject.addProperty("marketElement", term.getMarketFactorId());
			array.add(quotesObject);
		}
		seriesObject.add("terms", array);
		return seriesObject;
	}

	
}
