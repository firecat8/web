/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.servlet.esg;

import com.ers.v1.adapter.EsgAdapter;
import com.eurorisksystems.riskengine.ws.v1_1.vo.portfolio.evaluation.EvaluationIdVo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Plamen
 */
@WebServlet(name = "DeleteEvaluationServlet", urlPatterns = {"esg/deleteEval"})
public class DeleteEvaluationServlet extends HttpServlet {

    private final static Logger LOGGER = Logger.getLogger(DeleteEvaluationServlet.class.getCanonicalName());
    
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        String param = request.getParameter("evalId");
     
        EvaluationIdVo evalId = new EvaluationIdVo(param);
        EsgAdapter esgAdapter = new EsgAdapter();
        esgAdapter.setEvaluationId(evalId);
        PrintWriter writer = response.getWriter();
        try {
            esgAdapter.deleteEval();
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Error while deleting evaluation!");
            writer.write(gson.toJson("Could not delete evaluation!"));
        }
        writer.write(gson.toJson("Successfully deleted evaluation!"));
    }
    
    
    
    @Override
    public String getServletInfo() {
        return "Servlet used for deleting Evalutions. Called when ESG window is closed.";
    }

}
