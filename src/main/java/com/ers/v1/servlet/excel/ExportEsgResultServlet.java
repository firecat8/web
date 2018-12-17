/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.servlet.excel;

import com.ers.v1.adapter.SeriesAdapter;
import com.ers.v1.calc.esg.EsgObject;
import com.ers.v1.calc.esg.EsgResult;
import com.ers.v1.converter.EsgResultConverter;
import com.ers.v1.utils.ConverterUtils;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 *
 * @author gdimitrova
 */
@WebServlet(name = "ExportEsgResultServlet", urlPatterns = {"/exportEsgResults"})
public class ExportEsgResultServlet extends HttpServlet {

    private final static int HEADER_TABLE_ROW = 4;
    private final static int ESG_RESULT = 0;
    private final static int MF_NAME = 1;

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
        response.setHeader("Content-Disposition", "attachment; filename=ESG_Results.xls");
        response.setHeader("Cache-Control", "max-age=0");
        HSSFWorkbook wb = new HSSFWorkbook();
        try {
            writeInExcel(wb, request);
        } catch (ParseException | InterruptedException ex) {
            Logger.getLogger(ExportPredictionResultsServlet.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(505, ex.getMessage());
        }
        wb.write(response.getOutputStream());
    }

    private void writeInExcel(HSSFWorkbook wb, HttpServletRequest request) throws IOException, ParseException, InterruptedException {
        String requestStream = ConverterUtils.INSTANCE.convertToString(request.getInputStream());
        String decodedRequest = java.net.URLDecoder.decode(requestStream, "UTF-8");
        String[] pairs = decodedRequest.split("&");
        List<String> values = new ArrayList<>();
        for (String pair : pairs) {
            String[] pairResult = pair.split("=");
            if (pair.contains("seriesName") || pair.contains("esgResult")) {
                values.add(pairResult[1]);
            } else {
                values.set(ESG_RESULT, values.get(ESG_RESULT).concat(pairResult[0]));
            }
        }
        EsgResult esgResult = EsgResultConverter.INSTANCE.toObject(values.get(ESG_RESULT));
        Sheet sheet = wb.createSheet("ESG Results");
        writeEsgTotalInfo(wb, sheet, esgResult, values.get(MF_NAME));
        writeEsgTable(wb, sheet, esgResult);
    }

    private void writeEsgTable(HSSFWorkbook wb, Sheet sheet, EsgResult esgResult) {
        writeEsgTableHeader(wb, sheet);
        writeEsgTableResults(wb, sheet, esgResult);
    }

    private void writeEsgTableResults(HSSFWorkbook wb, Sheet sheet, EsgResult esgResult) {
        CreationHelper createHelper = wb.getCreationHelper();

        short format = createHelper.createDataFormat().getFormat("0.0000");
        wb.getCustomPalette().setColorAtIndex(HSSFColor.LIME.index, (byte) 219, (byte) 234, (byte) 245);
        short even = HSSFColor.LIME.index;
        short odd = HSSFColor.WHITE.index;
        CellStyle oddWeightStyle = getStyle(wb, odd, CellStyle.ALIGN_CENTER, format);
        CellStyle evenWeightStyle = getStyle(wb, even, CellStyle.ALIGN_CENTER, format);
        CellStyle oddCenterStyle = getStyle(wb, odd, CellStyle.ALIGN_CENTER);
        CellStyle evenCenterStyle = getStyle(wb, even, CellStyle.ALIGN_CENTER);
        CellStyle oddLeftStyle = getStyle(wb, odd, CellStyle.ALIGN_LEFT);
        CellStyle evenLeftStyle = getStyle(wb, even, CellStyle.ALIGN_LEFT);

        Map<EsgObject, Double> results = new HashMap<>();

        results.putAll(esgResult.getPositiveWeighted());
        results.putAll(esgResult.getNegativeWeighted());

        int resultRowIndex = HEADER_TABLE_ROW + 1;
        short color = odd;
        for (Map.Entry<EsgObject, Double> entry : results.entrySet()) {
            Row row = sheet.createRow(resultRowIndex);
            row.setHeight((short) 500);
            if (color == even) {
                addRowResult(row, entry, evenLeftStyle, evenWeightStyle, evenCenterStyle);
                color = odd;
            } else {
                addRowResult(row, entry, oddLeftStyle, oddWeightStyle, oddCenterStyle);
                color = even;
            }
            resultRowIndex++;
        }
        for (int i = 0;
                i < 6; i++) {
            sheet.autoSizeColumn(i, true);
        }
    }

    private CellStyle getStyle(HSSFWorkbook wb, short color, short align, short format) {
        CellStyle style = getStyle(wb, color, align);
        style.setDataFormat(format);
        return style;
    }

    private CellStyle getStyle(HSSFWorkbook wb, short color, short align) {
        CellStyle style = wb.createCellStyle();
        style.setAlignment(align);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFont(getNormalFont(wb, 10));
        style.setFillForegroundColor(color);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        setCellBorder(style);
        return style;
    }

    private void addRowResult(Row row, Map.Entry<EsgObject, Double> entry, CellStyle leftStyle, CellStyle weightStyle, CellStyle centerStyle) {
        EsgObject result = entry.getKey();
        Cell cell = row.createCell(0);
        cell.setCellValue(result.getMfId());
        cell.setCellStyle(leftStyle);
        cell = row.createCell(1);
        cell.setCellValue(result.getDescription());
        cell.setCellStyle(leftStyle);
        cell = row.createCell(2);
        cell.setCellValue(entry.getValue());
        cell.setCellStyle(weightStyle);
        cell = row.createCell(3);
        cell.setCellValue(result.getE());
        cell.setCellStyle(centerStyle);
        cell = row.createCell(4);
        cell.setCellValue(result.getS());
        cell.setCellStyle(centerStyle);
        cell = row.createCell(5);
        cell.setCellValue(result.getG());
        cell.setCellStyle(centerStyle);
    }

    private void writeEsgTableHeader(HSSFWorkbook wb, Sheet sheet) {
        Row headerTableRow = sheet.createRow(HEADER_TABLE_ROW);
        CellStyle headerColumnStyle = getStyle(wb, HSSFColor.PALE_BLUE.index, CellStyle.ALIGN_CENTER);
        headerColumnStyle.setFont(getBoldFont(wb, 10));
        headerTableRow.setHeight((short) 500);
        String[] headerNames = {"ESG index", "Description", " Weight ", " Env. Score ", " Social Score ", " Gov. Score "};
        for (int i = 0; i < headerNames.length; i++) {
            Cell cell = headerTableRow.createCell(i);
            cell.setCellStyle(headerColumnStyle);
            cell.setCellValue(headerNames[i]);
        }
    }

    private void writeEsgTotalInfo(HSSFWorkbook wb, Sheet sheet, EsgResult esgResult, String mfName) {
        writeText(wb, sheet, mfName, 0, 0, 2, getBoldFont(wb, 12));
        String line2 = "ESG Rating: " + esgResult.getEsgRating() + " Controversy: " + esgResult.getContorversy();
        writeText(wb, sheet, line2, 1, 0, 2, getBoldFont(wb, 12));
        String line3 = "Environment: " + esgResult.getE() + " Social: " + esgResult.getS() + " Governance: " + esgResult.getG();
        writeText(wb, sheet, line3, 2, 0, 2, getNormalFont(wb, 11));
    }

    private void writeText(HSSFWorkbook wb, Sheet sheet, String text, int rowIndex, int index, int cellCount, Font font) {
        Row row = sheet.createRow(rowIndex);
        CellStyle textStyle = wb.createCellStyle();
        textStyle.setAlignment(CellStyle.ALIGN_CENTER);
        textStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        textStyle.setFont(font);
        textStyle.setBorderTop(CellStyle.BORDER_THIN);
        textStyle.setTopBorderColor(HSSFColor.WHITE.index);
        row.setHeight((short) 400);

        for (int i = index; i < cellCount; i++) {
            row.createCell(i);
        }
        Cell cell = row.getCell(index);
        cell.setCellStyle(textStyle);
        cell.setCellValue(text);
        CellRangeAddress cellRangeAddress = new CellRangeAddress(rowIndex, rowIndex, 0, cellCount - 1);
        sheet.addMergedRegion(cellRangeAddress);
    }

    private void setCellBorder(CellStyle style) {
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setTopBorderColor(HSSFColor.BLACK.index);
    }

    private Font getBoldFont(HSSFWorkbook wb, int size) {
        Font font = wb.findFont(
                Font.BOLDWEIGHT_BOLD, Font.COLOR_NORMAL,
                (short) (size * 20), "Fontche", false, false, Font.SS_NONE, Font.U_NONE);

        if (font == null) {
            font = wb.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) size);
            font.setFontName("Fontche");
            font.setItalic(false);
            font.setStrikeout(false);
            font.setTypeOffset(Font.SS_NONE);
            font.setUnderline(Font.U_NONE);
        }
        return font;
    }

    private Font getNormalFont(HSSFWorkbook wb, int size) {
        Font font = wb.findFont(
                Font.BOLDWEIGHT_NORMAL, HSSFColor.BLACK.index,
                (short) (size * 20), "Fontche", false, false, Font.SS_NONE, Font.U_NONE);

        if (font == null) {
            font = wb.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
            font.setColor(HSSFColor.BLACK.index);
            font.setFontHeightInPoints((short) size);
            font.setFontName("Fontche");
            font.setItalic(false);
            font.setStrikeout(false);
            font.setTypeOffset(Font.SS_NONE);
            font.setUnderline(Font.U_NONE);
        }
        return font;
    }

}
