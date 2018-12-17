/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.converter;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import com.ers.v1.utils.ConverterUtils;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.MfiTermsWrapperVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.SeriesAdapterVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.multifactor.FormulaTermVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.multifactor.PowerFunctionVo;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * @author snyanakieva
 */
public class SeriesAdapterVoConverter implements JsonConverter<List<MfiTermsWrapperVo>> {

    public static SeriesAdapterVoConverter INSTANCE = new SeriesAdapterVoConverter();
    private final JsonParser JSON_PARSER = new JsonParser();

    @Override
    public String toString(List<MfiTermsWrapperVo> obj) {
        return convertoJson(obj).toString();
    }

    @Override
    public List<MfiTermsWrapperVo> toObject(String json) {
        List<MfiTermsWrapperVo> terms = new ArrayList<>();
        if (!json.isEmpty()) {
            JsonObject asJsonObject = JSON_PARSER.parse(json).getAsJsonObject();
            JsonArray jsonList = asJsonObject.get("series").getAsJsonArray();
            for (JsonElement element : jsonList) {
                MfiTermsWrapperVo term = new MfiTermsWrapperVo();
                JsonObject wrapperJsonObject = JSON_PARSER.parse(element.toString()).getAsJsonObject();
                term.setValue(
                        wrapperJsonObject.get("description").isJsonNull()
                        ? ""
                        : wrapperJsonObject.get("description").getAsString());
                SeriesAdapterVo seriesAdapterVo = new SeriesAdapterVo();
                seriesAdapterVo.setCorrelation(wrapperJsonObject.get("correlation").getAsDouble());
                seriesAdapterVo.setDistance(wrapperJsonObject.get("distance").getAsDouble());
                seriesAdapterVo.setEndDate(new GregorianCalendar());
                seriesAdapterVo.setStartDate(new GregorianCalendar());
                seriesAdapterVo.setSelected(true);
                seriesAdapterVo.setFormulaTerm(new FormulaTermVo(
                        wrapperJsonObject.get("marketElement").getAsString(),
                        wrapperJsonObject.get("termCoeficient").getAsDouble(),
                        new PowerFunctionVo()));
                seriesAdapterVo.setLength(wrapperJsonObject.get("length").getAsInt());
                seriesAdapterVo.setQuality(wrapperJsonObject.get("quality").getAsDouble());
                term.setKey(seriesAdapterVo);
                terms.add(term);
            }
        }
        return terms;
    }

    private JsonObject convertoJson(List<MfiTermsWrapperVo> wrappers) {
        JsonObject seriesObject = new JsonObject();
        JsonArray array = new JsonArray();
        for (MfiTermsWrapperVo wrapper : wrappers) {
            SeriesAdapterVo adapter = wrapper.getKey();
            JsonObject quotesObject = new JsonObject();
            quotesObject.addProperty("selected", adapter.isSelected());
            quotesObject.addProperty("termCoeficient", adapter.getFormulaTerm().getCoefficient());
            String function = adapter.getFormulaTerm().getFunction() != null
                    ? adapter.getFormulaTerm().getFunction().getClass().getSimpleName()
                    : null;
            quotesObject.addProperty("function", function);
            Double functionArgument = adapter.getFormulaTerm().getFunction() != null
                    && adapter.getFormulaTerm().getFunction() instanceof PowerFunctionVo
                    ? ((PowerFunctionVo) adapter.getFormulaTerm().getFunction()).getPower()
                    : null;
            quotesObject.addProperty("argument", functionArgument);
            quotesObject.addProperty("marketElement", adapter.getFormulaTerm().getMarketFactorId());
            quotesObject.addProperty("description", wrapper.getValue());
            quotesObject.add("startDate", ConverterUtils.INSTANCE.makeJsonDate(adapter.getStartDate()));
            quotesObject.add("endDate", ConverterUtils.INSTANCE.makeJsonDate(adapter.getEndDate()));
            quotesObject.addProperty("length", adapter.getLength());
            quotesObject.addProperty("quality", adapter.getQuality());
            quotesObject.addProperty("correlation", adapter.getCorrelation());
            quotesObject.addProperty("distance", adapter.getDistance());
            array.add(quotesObject);
        }
        seriesObject.add("series", array);
        return seriesObject;
    }

    private void setFormula(JsonObject quotesObject, SeriesAdapterVo adapter) {

    }
}
