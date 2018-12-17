/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.servlet.prediction;

import com.ers.v1.utils.ConverterUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author gdimitrova
 *
 * Delete only prediction results which are saved in the session
 */
public class DeletePredictionResultsServlet extends HttpServlet {

    private final JsonParser JSON_PARSER = new JsonParser();
    private final Gson gson = new GsonBuilder().create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(405, "Method GET Not Allowed");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8;");
        PrintWriter writer = response.getWriter();
        String evalID = JSON_PARSER.parse(
                ConverterUtils.INSTANCE.convertToStringFromOneReadLine(
                        request.getInputStream())).getAsString();
        HttpSession session = request.getSession();
        session.removeAttribute(evalID);
        writer.write(gson.toJson("Deleted prediction results"));
    }
}
