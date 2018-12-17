/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletInputStream;

import com.ers.v1.servlet.prediction.PredictionServlet;
import com.eurorisksystems.riskengine.ws.v1_1.vo.TenorVo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.charset.Charset;

/**
 *
 * @author snyanakieva
 */
public class ConverterUtils {

    public static ConverterUtils INSTANCE = new ConverterUtils();
	private final Gson gson = new GsonBuilder().serializeNulls().create();

    public String convertToString(ServletInputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        try {
            while (reader.ready()) {
                sb.append(reader.readLine());
            }
        } catch (IOException ex) {
            Logger.getLogger(PredictionServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }

    public TenorVo makeTenorVo(JsonObject obj) {
        return new TenorVo(
                obj.get("years").getAsInt(),
                obj.get("months").getAsInt(),
                obj.get("days").getAsInt()
        );
    }

    public String convertToStringFromOneReadLine(ServletInputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(reader.readLine());
        } catch (IOException ex) {
            Logger.getLogger(PredictionServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }

	public JsonElement makeJsonDate(Calendar calendar) {
		return gson.toJsonTree(
				calendar.get(Calendar.DAY_OF_MONTH) + "/"
				+ (calendar.get(Calendar.MONTH) + 1) + "/"
				+ calendar.get(Calendar.YEAR));
	}
}
