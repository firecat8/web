/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.servlet.excel;

import com.ers.v1.adapter.SeriesAdapter;
import com.ers.v1.entities.MarketFactorInfoHolder;
import com.ers.v1.parser.Parser;
import com.ers.v1.utils.ConverterUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 *
 * @author gdimitrova
 */
@WebServlet(name = "ExportPredictionresultsServlet", urlPatterns = {"/exportPredictResults"})
public class ExportPredictionResultsServlet extends HttpServlet {

    private final int SERIES_INDEX = 0;
    private final int EVALUATION_INDEX = 1;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=results.xls");
        response.setHeader("Cache-Control", "max-age=0");
        Workbook wb = new HSSFWorkbook();
        try {
            writeInExcel(wb, request);
        } catch (ParseException | InterruptedException ex) {
            Logger.getLogger(ExportPredictionResultsServlet.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(505, ex.getMessage());
        }
        wb.write(response.getOutputStream());
    }

    private void writeInExcel(Workbook wb, HttpServletRequest request) throws IOException, ParseException, InterruptedException {
        HttpSession session = request.getSession();
        String requestStream = ConverterUtils.INSTANCE.convertToString(
                request.getInputStream());
        String decodedRequest = java.net.URLDecoder.decode(requestStream, "UTF-8");
        String[] pairs = decodedRequest.split("&");
        List<String> values = new ArrayList<>();
        for (String pair : pairs) {
            values.add(pair.split("=")[1]);
        }
        JsonArray requestArray = (JsonArray) session.getAttribute(values.get(EVALUATION_INDEX));
        DateFormat dateFormat = new SimpleDateFormat(Parser.SIMPLE_DATE_FORMAT);
        Sheet sheet = wb.createSheet("Results");
        CreationHelper createHelper = wb.getCreationHelper();
        MarketFactorInfoHolder holder = (MarketFactorInfoHolder)session.getAttribute(values.get(SERIES_INDEX));
        setSeriesName(wb, sheet, holder.getFilename());
        setHeaders(wb, sheet);
        addRows(wb, sheet, createHelper, dateFormat, requestArray);
    }

    private void setHeaders(Workbook wb, Sheet sheet) {
        Row headerTableRow = sheet.createRow(2);
        CellStyle headerColumnStyle = wb.createCellStyle();
        headerColumnStyle.setAlignment(CellStyle.ALIGN_LEFT);
        headerColumnStyle.setFont(getHeaderFont(wb));
        headerTableRow.setRowStyle(headerColumnStyle);
        headerTableRow.setHeight((short) 400);
        Cell cell = headerTableRow.createCell(0);
        cell.setCellStyle(headerColumnStyle);
        cell.setCellValue("Date");
        cell = headerTableRow.createCell(1);
        cell.setCellStyle(headerColumnStyle);
        cell.setCellValue("Historical Prices");
        cell = headerTableRow.createCell(2);
        cell.setCellStyle(headerColumnStyle);
        cell.setCellValue("Predictions");
        cell = headerTableRow.createCell(3);
        cell.setCellStyle(headerColumnStyle);
        cell.setCellValue("volaPlus");
        cell = headerTableRow.createCell(4);
        cell.setCellStyle(headerColumnStyle);
        cell.setCellValue("volaMinus");
    }

    private Font getHeaderFont(Workbook wb) {
        Font font = wb.findFont(
                Font.BOLDWEIGHT_NORMAL, Font.COLOR_NORMAL,
                (short) (11 * 20), "HeaderFont", false, false, Font.SS_NONE, Font.U_NONE);

        if (font == null) {
            font = wb.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 11);
            font.setFontName("HeaderFont");
            font.setItalic(false);
            font.setStrikeout(false);
            font.setTypeOffset(Font.SS_NONE);
            font.setUnderline(Font.U_NONE);
        }
        return font;
    }

    private void addRows(Workbook wb, Sheet sheet, CreationHelper createHelper, DateFormat dateFormat, JsonArray resultsArray) throws ParseException {
        int rowsSize = resultsArray.size();

        CellStyle dateStyle = wb.createCellStyle();
        dateStyle.setAlignment(CellStyle.ALIGN_LEFT);
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat(Parser.SIMPLE_DATE_FORMAT));
        dateStyle.setFont(getDefaultFont(wb));

        CellStyle numberStyle = wb.createCellStyle();
        numberStyle.setAlignment(CellStyle.ALIGN_RIGHT);
        numberStyle.setDataFormat(createHelper.createDataFormat().getFormat("0.00"));
        numberStyle.setFont(getDefaultFont(wb));
        DateFormat resultDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String[] resultsNames = {"historicalPrices", "predictions", "volaPlus", "volaMinus"};
        for (int i = 0; i < rowsSize; i++) {
            JsonObject result = resultsArray.get(i).getAsJsonObject();
            Calendar date = toCalendar(result.get("date").getAsString(), resultDateFormat, dateFormat);

            Row row = sheet.createRow(i + 3);
            row.setRowStyle(numberStyle);
            row.setHeight((short) 400);
            Cell cell = row.createCell(0);
            cell.setCellStyle(dateStyle);
            cell.setCellValue(date);
            for (int j = 0; j < resultsNames.length; j++) {
                cell = row.createCell(j + 1);
                cell.setCellStyle(numberStyle);
                String saveNullResult = getSaveNullResult(result.get(resultsNames[j]));
                if (saveNullResult.equals("-")) {
                    cell.setCellValue(saveNullResult);
                } else {
                    cell.setCellValue(Double.parseDouble(saveNullResult));
                }
            }
        }
        for (int i = 0; i < 5; i++) {
            sheet.setColumnWidth(i, 40 * 200);
        }
    }

    private Font getDefaultFont(Workbook wb) {
        Font font = wb.findFont(
                Font.BOLDWEIGHT_NORMAL, Font.COLOR_NORMAL,
                (short) (10 * 20), "DefaultFont", false, false, Font.SS_NONE, Font.U_NONE);
        if (font == null) {
            font = wb.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("DefaultFont");
            font.setItalic(false);
            font.setStrikeout(false);
            font.setTypeOffset(Font.SS_NONE);
            font.setUnderline(Font.U_NONE);
        }
        return font;
    }

    private Calendar toCalendar(String resultDate, DateFormat resultDateFormat, DateFormat dateFormat) throws ParseException {
        Date date = convertDateFormat(resultDate, resultDateFormat, dateFormat);
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(date.getTime());
        return calendar;
    }

    private String getSaveNullResult(JsonElement result) {
        if (result.isJsonNull()) {
            return "-";
        }
        return result.toString();
    }

    private void setSeriesName(Workbook wb, Sheet sheet, String name) throws InterruptedException {
       
        Row serieNameRow = sheet.createRow(0);
        CellStyle seriesNameStyle = wb.createCellStyle();
        seriesNameStyle.setAlignment(CellStyle.ALIGN_CENTER);
        seriesNameStyle.setFont(getHeaderFont(wb));
        serieNameRow.setRowStyle(seriesNameStyle);
        serieNameRow.setHeight((short) 400);

        for (int i = 0; i < 5; i++) {
            serieNameRow.createCell(i);
        }
        Cell cell = serieNameRow.getCell(0);
        cell.setCellStyle(seriesNameStyle);
        cell.setCellValue(name);
        CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, 0, 4);
        sheet.addMergedRegion(cellRangeAddress);
    }

    private Date convertDateFormat(String resultDate, DateFormat resultDateFormat, DateFormat dateFormat) throws ParseException {
        Date date = resultDateFormat.parse(resultDate);
        dateFormat.format(date);
        return date;
    }
}
