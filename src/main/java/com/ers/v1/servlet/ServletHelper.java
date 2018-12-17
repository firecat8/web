/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.servlet;

import com.ers.v1.servlet.prediction.PredictionServlet;
import com.ers.v1.adapter.Adapter;
import com.eurorisksystems.riskengine.ws.v1_1.vo.ErrorVo;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author gdimitrova
 */
public class ServletHelper {

    public boolean checkErrors(Adapter adapter, HttpServletResponse response) {
        Collection<ErrorVo> errors = adapter.getErrors();
        if (!errors.isEmpty()) {
            try {
                response.sendError(500, buildErrorsResponse(errors));
                return true;
            } catch (IOException ex) {
                Logger.getLogger(PredictionServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    private String buildErrorsResponse(Collection<ErrorVo> errors) {
        StringBuilder sb = new StringBuilder();
        errors.forEach((error) -> {
            List<String> errorsDescription = error.getErrors();
            sb.append(error.getErrorCode().value()).append(":<br>");
            for (int i = 0; i < errorsDescription.size(); i++) {
                String get = errorsDescription.get(i);
                sb.append(i+1).append(") ")
                        .append(errorsDescription.get(i))
                        .append("<br>");
            }
        });
        return sb.toString();
    }
}
