/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.servlet.prediction;

import com.ers.v1.adapter.PredictionAdapter;
import com.ers.v1.converter.PredictionConfigVoConverter;
import com.ers.v1.converter.PredictionResultsConverter;
import com.ers.v1.entities.MarketFactorInfoHolder;
import com.ers.v1.servlet.ServletHelper;
import com.eurorisksystems.riskengine.ws.v1_1.vo.ServiceStatusVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.StateVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.market.factor.InstrumentMarketFactorVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.prediction.PredictionConfigVo;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.servlet.annotation.WebServlet;

import com.ers.v1.utils.ConverterUtils;
import com.eurorisksystems.riskengine.ws.v1_1.vo.portfolio.evaluation.EvaluationIdVo;
import com.google.gson.JsonSyntaxException;
import javax.servlet.http.HttpSession;

/**
 *
 * @author gdimitrova
 */
@WebServlet(name = "PredictionServlet", urlPatterns = {"/predict"})
public class PredictionServlet extends HttpServlet {

    private final static Logger LOGGER = Logger.getLogger(PredictionServlet.class.getCanonicalName());
    private final JsonParser JSON_PARSER = new JsonParser();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(405, "Method GET Not Allowed");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            PredictionAdapter adapter = new PredictionAdapter();
            response.setContentType("application/json;charset=UTF-8;");
            PrintWriter writer = response.getWriter();
            ServletHelper servletHelper = new ServletHelper();

            JsonObject inputJsonObj = JSON_PARSER.parse(
                    ConverterUtils.INSTANCE.convertToStringFromOneReadLine(
                            request.getInputStream())).getAsJsonObject();

            if (inputJsonObj.get("mfId") == null || inputJsonObj.get("mfId").getAsString().equals("")) {
                response.sendError(400, "Please upload Excel file.");
                return;
            }
            String mfId = inputJsonObj.get("mfId").getAsString();
            HttpSession session = request.getSession();
            MarketFactorInfoHolder marketFactorInfoHolder = (MarketFactorInfoHolder) session.getAttribute(mfId);
            InstrumentMarketFactorVo marketFactorVo = marketFactorInfoHolder.getMarketFactorVo();
            String underlyingId = marketFactorVo.getInstrumentId();

            if (underlyingId == null) {
                response.sendError(404, "Not found series!");
                return;
            }
            if ("dummy".equals(underlyingId) || !underlyingId.contains("CommodityINEA_")) {
                adapter.createInstrument(mfId);
                if (servletHelper.checkErrors(adapter, response)) {
                    return;
                }
                marketFactorVo = adapter.getMarketFactorVo();
                session.setAttribute(mfId, new MarketFactorInfoHolder(marketFactorInfoHolder.getFilename(), marketFactorVo,marketFactorInfoHolder.getQuotes()));
            }else{
                adapter.getMarketFactor(mfId);
            }
            
            final String evalId = startPrediction(inputJsonObj.get("config").toString(), adapter);
            if (servletHelper.checkErrors(adapter, response)) {
                return;
            }
            String predictResultsJson = PredictionResultsConverter.INSTANCE.toString(adapter.getResults());
            JsonObject resultsJsonObject = new JsonParser()
                    .parse(predictResultsJson)
                    .getAsJsonObject();
            session.setAttribute(evalId,
                    resultsJsonObject.get("results").getAsJsonArray());

            resultsJsonObject.addProperty("evalID", evalId);
            resultsJsonObject.addProperty("seriesID", marketFactorVo.getId());

            writer.write(resultsJsonObject.toString());
        } catch (JsonSyntaxException | IOException | InterruptedException ex) {
            response.sendError(500, ex.getMessage());
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void checkStatus(PredictionAdapter adapter) throws InterruptedException {
        ServiceStatusVo status;
        do {
            TimeUnit.SECONDS.sleep(5);
            status = adapter.checkStatus();
            if (status == null) {
                break;
            }
            LOGGER.log(Level.INFO, " {0} {1}", new Object[]{status.getDescription(), status.getState().value()});
        } while (status.getState() == StateVo.WORKING);
    }

    private String startPrediction(String config, PredictionAdapter adapter) throws InterruptedException {
        PredictionConfigVo convertToPredictionConfigVo = PredictionConfigVoConverter.INSTANCE.toObject(config);
        EvaluationIdVo evalId = adapter.createEvaluation(convertToPredictionConfigVo);
        checkStatus(adapter);
        adapter.predictFundPrice();
        checkStatus(adapter);
        adapter.getPredictionResults();
        adapter.deleteEval();
        return evalId.getName();
    }

}
