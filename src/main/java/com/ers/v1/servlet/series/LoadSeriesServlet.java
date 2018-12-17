/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.servlet.series;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ers.v1.adapter.SeriesAdapter;
import com.ers.v1.converter.MarketFactorQuotesVoConverter;
import com.eurorisksystems.riskengine.ws.v1_1.vo.descriptions.MarketFactorQuoteDescriptionVo;

/**
 *
 * @author snayanakieva
 */
@WebServlet(name = "LoadSeriesServlet", urlPatterns = {"/loadSeries"})
public class LoadSeriesServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String paramValue = request.getParameter("mfId");
			String start = request.getParameter("start");
			Calendar startDate = Calendar.getInstance();
			if (start != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
				Date d = sdf.parse(start);

				startDate.setTime(d);
			} else {
				startDate = new GregorianCalendar(2010, 1, 1);
			}
			PrintWriter writer = response.getWriter();
			SeriesAdapter seriesAdapter = new SeriesAdapter();
			List<MarketFactorQuoteDescriptionVo> quotes = seriesAdapter.loadSeries(paramValue, startDate);

			if (quotes == null) {
				response.sendError(505, "Error while Loading series!");
			}
			writer.write(MarketFactorQuotesVoConverter.INSTANCE.toString(quotes));
		} catch (ParseException ex) {
			Logger.getLogger(LoadSeriesServlet.class.getName()).log(Level.SEVERE, null, ex);
			response.sendError(505, "Error while Loading series!");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		throw new UnsupportedOperationException();
	}

}
