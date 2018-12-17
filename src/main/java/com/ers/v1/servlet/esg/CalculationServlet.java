/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.servlet.esg;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ers.v1.calc.esg.EsgCalculator;
import com.ers.v1.calc.esg.EsgObject;
import com.ers.v1.calc.esg.EsgResult;
import com.ers.v1.converter.EsgResultConverter;
import com.ers.v1.converter.SeriesAdapterVoConverter;
import com.ers.v1.reader.EsgFactorsReader;
import com.ers.v1.utils.ConverterUtils;

/**
 *
 * @author snyanakieva
 */
@WebServlet(name = "CalculationServlet", urlPatterns = {"/esg/calculate"})
public class CalculationServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json;charset=UTF-8;");
		EsgFactorsReader reader = new EsgFactorsReader();
		EsgCalculator calculator = new EsgCalculator();
		PrintWriter writer = response.getWriter();

		String jsonString = ConverterUtils.INSTANCE.convertToString(request.getInputStream());
		List terms = SeriesAdapterVoConverter.INSTANCE.toObject(jsonString);
		List termsCopy = new ArrayList(terms);
		reader.readFromStream(getClass().getResourceAsStream("/ESG.xls"));
		List<EsgObject> factors = reader.getEsgFactors();
		EsgResult result = calculator.calculate(termsCopy, factors);
		writer.write(EsgResultConverter.INSTANCE.toString(result));
	}

}
