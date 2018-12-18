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
import com.ers.v1.converter.FactorsSelectionCfgConverter;
import com.ers.v1.converter.SeriesAdapterVoConverter;
import com.ers.v1.entities.MarketFactorInfoHolder;
import com.ers.v1.servlet.prediction.PredictionServlet;
import com.ers.v1.servlet.ServletHelper;
import com.ers.v1.utils.ConverterUtils;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.MfiTermsWrapperVo;
import javax.servlet.http.HttpSession;

/**
 *
 * @author snyanakieva
 */
@WebServlet(name = "LoadFactorsServlet", urlPatterns = {"/esg/loadFactors"})
public class LoadFactorsServlet extends HttpServlet {

    private final static Logger LOGGER = Logger.getLogger(PredictionServlet.class.getCanonicalName());

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
            ServletHelper servletHelper = new ServletHelper();
            String stream = ConverterUtils.INSTANCE.convertToStringFromOneReadLine(request.getInputStream());
            FactorsSelectionConfig cfg = FactorsSelectionCfgConverter.INSTANCE.toObject(stream);
            if (cfg.getSeriesName().equals("")) {
                response.sendError(400, "Please upload Excel file with serie.");
                return;
            }
            HttpSession session = request.getSession();
            MarketFactorInfoHolder holder = (MarketFactorInfoHolder) session.getAttribute(cfg.getSeriesId());
            adapter.loadFactors(cfg, holder.getMarketFactorVo(), true);
            if (servletHelper.checkErrors(adapter, response)) {
                return;
            }
            adapter.getFactors();
            List<MfiTermsWrapperVo> series = adapter.getSeries();
            writer.write(SeriesAdapterVoConverter.INSTANCE.toString(series));

        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            response.sendError(505, "Error loading factors.");
        }
    }

}
