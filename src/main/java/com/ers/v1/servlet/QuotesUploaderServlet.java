/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ers.v1.servlet;

import com.ers.v1.adapter.SeriesAdapter;
import com.ers.v1.entities.MarketFactorInfoHolder;
import com.ers.v1.reader.QuoteExcelReader;
import com.ers.v1.reader.exceptions.InvalidFileExtensionException;
import com.ers.v1.reader.exceptions.InvalidSheetFormatException;
import com.ers.v1.reader.exceptions.UnableToParseDateException;
import com.ers.v1.servlet.exceptions.InvalidUploadRequestException;
import com.eurorisksystems.riskengine.ws.v1_1.vo.market.factor.InstrumentMarketFactorVo;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author gdimitrova
 */
@WebServlet(name = "QuotesUploaderServlet", urlPatterns = {"/quotesUploader"})
@MultipartConfig
public class QuotesUploaderServlet extends HttpServlet {

    private final static Logger LOGGER = Logger.getLogger(QuotesUploaderServlet.class.getCanonicalName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        SeriesAdapter seriesAdapter = new SeriesAdapter();
        JsonObject inputJsonObj = new JsonObject();
        try {
            checkRequest(request);
            Part filePart = request.getPart("file");
            String file = Paths.get(request.getPart("file").getSubmittedFileName()).getFileName().toString();
            String filename = FilenameUtils.removeExtension(file);
            String mfName = filename;
            checkFileExtension(file);

            QuoteExcelReader reader = new QuoteExcelReader();
            reader.readFromStream(filePart.getInputStream());
            if (reader.getContents().isEmpty()) {
                sendResponse(response, inputJsonObj, file, Boolean.FALSE, "Quotes not found in file!");
                return;
            }
            seriesAdapter.saveSeries(mfName, reader.getContents());
            InstrumentMarketFactorVo marketFactorVo = seriesAdapter.getMarketFactorVo();
            LOGGER.log(Level.INFO, "Market Factor ID: {0}", marketFactorVo.getId());
            if (marketFactorVo.getId() == null) {
                sendResponse(response, inputJsonObj, file, Boolean.FALSE, "Quotes not found !");
                return;
            }

            HttpSession session = request.getSession();
            TreeMap<Calendar, Double> sorted = new TreeMap<>();
            sorted.putAll(reader.getContents());
            session.setAttribute(marketFactorVo.getId(), new MarketFactorInfoHolder(filename, marketFactorVo, sorted));

            sendResponse(response, inputJsonObj, file, Boolean.TRUE, makeSuccessJsonObject(marketFactorVo.getId(), mfName));
        } catch (IllegalArgumentException|InterruptedException | InvalidSheetFormatException| UnableToParseDateException  ex) {
            Logger.getLogger(QuotesUploaderServlet.class.getName()).log(Level.SEVERE, null, ex);
            inputJsonObj.addProperty("state", Boolean.FALSE);
            inputJsonObj.addProperty("extra", ex.getMessage());
            response.setStatus(400);
            response.getWriter().write(inputJsonObj.toString());
        }
    }

    private void checkRequest(HttpServletRequest request) throws IOException, ServletException {
        boolean containsFile = request.getPart("file").getSize() > 0;
        if (!ServletFileUpload.isMultipartContent(request) || !containsFile) {
            throw new InvalidUploadRequestException();
        }
        LOGGER.log(Level.INFO, "contains file: {0}", containsFile);
    }

    private void checkFileExtension(String filename) throws InvalidFileExtensionException {
        String fileExtension = FilenameUtils.getExtension(filename);
        LOGGER.log(Level.INFO, fileExtension);
        if (!fileExtension.equals("xlsx") && !fileExtension.equals("xls")) {
            throw new InvalidFileExtensionException();
        }
        LOGGER.log(Level.INFO, "File extension is valid.");
    }

    private void sendResponse(
            HttpServletResponse response, JsonObject inputJsonObj,
            String file, Boolean state, String extra)
            throws IOException {
        Writer writer = response.getWriter();
        inputJsonObj.addProperty("name", file);
        inputJsonObj.addProperty("state", state);
        inputJsonObj.addProperty("extra", extra);
        writer.write(inputJsonObj.toString());
    }

    private void sendResponse(
            HttpServletResponse response, JsonObject inputJsonObj,
            String file, Boolean state, JsonObject extra)
            throws IOException {
        Writer writer = response.getWriter();
        inputJsonObj.addProperty("name", file);
        inputJsonObj.addProperty("state", state);
        inputJsonObj.add("extra", extra);
        writer.write(inputJsonObj.toString());
    }

    private JsonObject makeSuccessJsonObject(String mfId, String mfName) {
        JsonObject success = new JsonObject();
        success.addProperty("mfId", mfId);
        success.addProperty("mfName", mfName);
        return success;
    }

}
