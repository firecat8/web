/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.servlet.series;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ers.v1.adapter.SeriesAdapter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 *
 * @author snayanakieva
 */
@WebServlet(name = "GetFactorsServlet", urlPatterns = {"/getFactors"})
public class GetFactorsServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		new SeriesAdapter().loadIneaFactors();
		PrintWriter writer = response.getWriter();
		JsonArray resultsArray = new JsonArray();
		for (String factorName : SeriesAdapter.MARKET_FACTORS.keySet()) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("value", SeriesAdapter.MARKET_FACTORS.get(factorName));
			jsonObject.addProperty("text", factorName.substring(0, factorName.length() - 4));
			resultsArray.add(jsonObject);
		}
		writer.write(resultsArray.toString());
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		throw new UnsupportedOperationException();
	}
	
}
