/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ers.v1.servlet.series;

import com.ers.v1.adapter.PredictionAdapter;
import com.ers.v1.adapter.SeriesAdapter;
import com.ers.v1.entities.MarketFactorInfoHolder;
import com.ers.v1.servlet.ServletHelper;
import com.ers.v1.servlet.prediction.PredictionServlet;
import com.ers.v1.utils.ConverterUtils;
import com.eurorisksystems.riskengine.ws.v1_1.vo.market.factor.InstrumentMarketFactorVo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author gdimitrova
 */
public class DeleteSeriesServlet extends HttpServlet {

    private final static Logger LOGGER = Logger.getLogger(DeleteSeriesServlet.class.getCanonicalName());
    private final JsonParser JSON_PARSER = new JsonParser();
    private final Gson gson = new GsonBuilder().create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8;");
        PrintWriter writer = response.getWriter();
        ServletHelper servletHelper = new ServletHelper();
        try {
            String mfId = JSON_PARSER.parse(
                    ConverterUtils.INSTANCE.convertToStringFromOneReadLine(
                            request.getInputStream())).getAsString();
            if (mfId == null || mfId.equals("")) {
                response.sendError(400, "Market factor id can't be null or empty!");
                return;
            }
            HttpSession session = request.getSession();
            MarketFactorInfoHolder holder = (MarketFactorInfoHolder) session.getAttribute(mfId);
            InstrumentMarketFactorVo marketFactorVo = holder.getMarketFactorVo();

            SeriesAdapter seriesAdapter = new SeriesAdapter();
            seriesAdapter.deleteSeries(marketFactorVo,holder.getQuotes());

            if (servletHelper.checkErrors(seriesAdapter, response)) {
                return;
            }

            //no matter which calculation adapter to use for deleting of instrument
            PredictionAdapter predictionAdapter = new PredictionAdapter();
            predictionAdapter.deleteInstrument(marketFactorVo.getInstrumentId());
            if (servletHelper.checkErrors(predictionAdapter, response)) {
                return;
            }
            session.removeAttribute(mfId);
            writer.write(gson.toJson("Deleted  ".concat(marketFactorVo.getId())));

        } catch (InterruptedException ex) {
            Logger.getLogger(DeleteSeriesServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
