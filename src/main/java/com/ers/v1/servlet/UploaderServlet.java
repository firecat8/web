/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.servlet;

import com.ers.v1.adapter.SeriesAdapter;
import com.ers.v1.converter.MarketFactorQuotesVoConverter;
import com.ers.v1.reader.exceptions.InvalidFileExtensionException;
import com.ers.v1.reader.exceptions.InvalidSheetFormatException;
import com.ers.v1.reader.QuoteExcelReader;
import com.ers.v1.reader.exceptions.UnableToParseDateException;
import com.ers.v1.servlet.exceptions.InvalidUploadRequestException;
import com.eurorisksystems.riskengine.ws.v1_1.vo.descriptions.MarketFactorQuoteDescriptionVo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

import com.google.gson.JsonObject;

/**
 *
 * @author Plamen
 */
@WebServlet(name = "UploaderServlet", urlPatterns = {"/uploader"})
@MultipartConfig
public class UploaderServlet extends HttpServlet {

    private final static Logger LOGGER = Logger.getLogger(UploaderServlet.class.getCanonicalName());

    private final SeriesAdapter seriesAdapter = new SeriesAdapter();

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
        request.setCharacterEncoding("UTF-8");
        JsonObject inputJsonObj = new JsonObject();
        try {
            validateRequestContent(request);

            Part filePart = request.getPart("file");
            String filename = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            String parameter = FilenameUtils.removeExtension(filename);
            processFile(filePart, parameter);

            response.setContentType("application/json");
            inputJsonObj.addProperty("name", filename);
            sendResponse(response, parameter, inputJsonObj);

        } catch (InvalidSheetFormatException | UnableToParseDateException
                | InvalidFileExtensionException | InvalidUploadRequestException exception) {
            LOGGER.log(Level.SEVERE, exception.getMessage());
            inputJsonObj.addProperty("state", Boolean.FALSE);
            inputJsonObj.addProperty("extra", exception.getMessage());
            response.setStatus(400);
            response.getWriter().write(inputJsonObj.toString());
        }
    }

    private void validateRequestContent(HttpServletRequest request)
            throws IOException, ServletException, InvalidUploadRequestException {
        boolean containsFile = request.getPart("file").getSize() > 0;
        if (!ServletFileUpload.isMultipartContent(request) || !containsFile) {
            throw new InvalidUploadRequestException();
        }
        LOGGER.log(Level.INFO, "contains file: {0}", containsFile);
    }

    private void processFile(Part filePart, String marketFactorName)
            throws InvalidSheetFormatException, UnableToParseDateException, InvalidFileExtensionException, IOException {

        checkFileExtension(filePart);

        QuoteExcelReader reader = new QuoteExcelReader();
        reader.readFromStream(filePart.getInputStream());
        try {
            seriesAdapter.saveSeries(marketFactorName, reader.getContents());
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void sendResponse(HttpServletResponse response, String mfName, JsonObject inputJsonObj) throws IOException {
        Writer writer = response.getWriter();
        String mfId = SeriesAdapter.MARKET_FACTORS.get(mfName + "INEA");
        LOGGER.log(Level.INFO, "Market Factor ID: {0}", mfId);
        List<MarketFactorQuoteDescriptionVo> quotesDesc = seriesAdapter.loadSeries(mfId);

        if (quotesDesc != null) {
            inputJsonObj.addProperty("state", Boolean.TRUE);
            inputJsonObj.add("extra", MarketFactorQuotesVoConverter.INSTANCE.convertToJsonArray(quotesDesc, "quotes"));
            writer.write(inputJsonObj.toString());
            return;
        }
        inputJsonObj.addProperty("state", Boolean.FALSE);
        inputJsonObj.addProperty("extra", "Quotes not found!");
        response.setStatus(404);
        response.getWriter().write(inputJsonObj.toString());
    }

    private void checkFileExtension(final Part filePart) throws InvalidFileExtensionException {
        String fileExtension = FilenameUtils.getExtension(getFileName(filePart));
        LOGGER.log(Level.INFO, fileExtension);
        if (fileExtension.equals("xlsx") || fileExtension.equals("xls")) {
            LOGGER.log(Level.INFO, "File extension is valid.");
        } else {
            throw new InvalidFileExtensionException();
        }
    }

    private String getFileName(final Part part) {
        final String partHeader = part.getHeader("content-disposition");
        LOGGER.log(Level.INFO, "Part Header = {0}", partHeader);
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

}
