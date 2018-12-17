/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.servlet.esg;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ers.v1.adapter.EsgAdapter;
import com.ers.v1.servlet.prediction.PredictionServlet;
import com.eurorisksystems.riskengine.ws.v1_1.vo.portfolio.evaluation.EvaluationIdVo;
import com.google.gson.JsonObject;

/**
 *
 * @author snyanakieva
 */
@WebServlet(name = "CreateEvaluationServlet", urlPatterns = {"/esg/createEvaluation"})
public class CreateEvaluationServlet extends HttpServlet {

	private final static Logger LOGGER = Logger.getLogger(PredictionServlet.class.getCanonicalName());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			EsgAdapter adapter = new EsgAdapter();
			EvaluationIdVo evalid = adapter.createEvaluation();
			if (!adapter.getErrors().isEmpty()) {
				throw new InterruptedException();
			}
			JsonObject responseJson = new JsonObject();
			responseJson.addProperty("evalId", evalid.getName());
			response.getWriter().write(responseJson.toString());
			

		} catch (InterruptedException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
			response.sendError(505, "Error creating evaluation.");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		throw new UnsupportedOperationException();
	}
}
