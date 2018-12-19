/*
 * 
 *  EuroRisk Systems (c) Ltd. All rights reserved.
 * 
 */
package com.ers.v1.servlet.series;

import com.ers.v1.adapter.SeriesAdapter;
import com.ers.v1.converter.MarketFactorQuotesVoConverter;
import com.ers.v1.entities.MarketFactorInfoHolder;
import com.ers.v1.utils.ConverterUtils;
import com.eurorisksystems.riskengine.ws.v1_1.vo.descriptions.MarketFactorQuoteDescriptionVo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author gdimitrova
 */
@WebServlet(name = "LoadSessionQuotesServlet", urlPatterns = {"/loadQuotes"})
public class LoadSessionQuotesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mfId = request.getParameter("mfId");
        PrintWriter writer = response.getWriter();
        HttpSession session = request.getSession();
        MarketFactorInfoHolder marketFactorInfoHolder = (MarketFactorInfoHolder) session.getAttribute(mfId);
        JsonObject quotes = convert(marketFactorInfoHolder.getQuotes());
        writer.write(quotes.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new UnsupportedOperationException();
    }

    private JsonObject convert(Map<Calendar, Double> quotes) {
         JsonObject quotesObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        List<Calendar> calendars = new ArrayList<>(quotes.keySet());
        Collections.sort(calendars) ;
        for (Calendar calendar : calendars) {
            JsonObject jsonResult = new JsonObject();
            jsonResult.add("date", ConverterUtils.INSTANCE.makeJsonDate(calendar));
            jsonResult.addProperty("value", quotes.get(calendar));
            jsonArray.add(jsonResult);
        }
        quotesObject.add("quotes", jsonArray);
        return quotesObject;
    }
}
