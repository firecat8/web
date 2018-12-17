/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.servlet.esg;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ers.v1.adapter.EsgAdapter;
import com.ers.v1.converter.FormulaTermsConverter;
import com.ers.v1.converter.MarketFactorQuotesVoConverter;
import com.ers.v1.servlet.prediction.PredictionServlet;
import com.ers.v1.utils.ConverterUtils;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.GeneratedSeriesVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.multifactor.FormulaTermVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.multifactor.MultiFactorInstrumentVo;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * @author snyanakieva
 */
@WebServlet(name = "FindFormulaServlet", urlPatterns = {"/esg/findFormula"})
public class FindFormulaServlet extends HttpServlet {

	private final static Logger LOGGER = Logger.getLogger(PredictionServlet.class.getCanonicalName());
	private final JsonParser JSON_PARSER = new JsonParser();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			EsgAdapter adapter = new EsgAdapter();
			response.setContentType("application/json;charset=UTF-8;");
			PrintWriter writer = response.getWriter();
			String jsonString = ConverterUtils.INSTANCE.convertToString(request.getInputStream());
			JsonObject asJsonObject = JSON_PARSER.parse(jsonString).getAsJsonObject();
			String evalId = asJsonObject.get("evalId").getAsString();
			List terms = FormulaTermsConverter.INSTANCE.toObject(jsonString);
			adapter.findFormula(terms, evalId);
			adapter.getCalibratedMfi(evalId);
			MultiFactorInstrumentVo mfi = adapter.getMfi();
			adapter.generateSeries();
			adapter.loadGeneratedSeries();
			writer.write(makeFindFormulaResponse(mfi.getModels().get(0).getTerms(), adapter.getGeneratedSeries()));

		} catch (InterruptedException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
			response.sendError(505, "Error finding formula.");
		}
	}

	private String makeFindFormulaResponse(List<FormulaTermVo> terms, GeneratedSeriesVo generatedSeries) {
		JsonObject object = FormulaTermsConverter.INSTANCE.convertoJson(terms);
		object.add("generatedPrices", MarketFactorQuotesVoConverter.INSTANCE.convertQuotesToJsonArray(generatedSeries.getGeneratedPrices().getMarketFactorQuotes()));
		object.add("targetPrices", MarketFactorQuotesVoConverter.INSTANCE.convertQuotesToJsonArray(generatedSeries.getTargetPrices().getMarketFactorQuotes()));
		return object.toString();
	}
}
