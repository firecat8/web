/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.converter;

import java.util.HashMap;
import java.util.Map;

import com.ers.v1.calc.esg.EsgObject;
import com.ers.v1.calc.esg.EsgResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * @author snyanakieva
 */
public class EsgResultConverter implements JsonConverter<EsgResult> {

    private final JsonParser JSON_PARSER = new JsonParser();
    public static EsgResultConverter INSTANCE = new EsgResultConverter();

    @Override
    public String toString(EsgResult obj) {
        JsonObject esgFactors = new JsonObject();
        Map<EsgObject, Double> allFactors = new HashMap<>();
        allFactors.putAll(obj.getPositiveWeighted());
        allFactors.putAll(obj.getNegativeWeighted());
        esgFactors.add("factors", convertCollection(allFactors));
        esgFactors.addProperty("e", obj.getE());
        esgFactors.addProperty("s", obj.getS());
        esgFactors.addProperty("g", obj.getG());
        esgFactors.addProperty("esgRating", obj.getEsgRating());
        esgFactors.addProperty("controversy", obj.getContorversy());
        return esgFactors.toString();
    }

    @Override
    public EsgResult toObject(String json) {
        JsonObject asJsonObject = JSON_PARSER.parse(json).getAsJsonObject();
        Map<EsgObject, Double> positiveWeighted = new HashMap<>();
        Map<EsgObject, Double> negativeWeighted = new HashMap<>();
        fillCollections(asJsonObject.get("factors").getAsJsonArray(), positiveWeighted, negativeWeighted);
        return new EsgResult(
                positiveWeighted,
                negativeWeighted,
                asJsonObject.get("e").getAsInt(),
                asJsonObject.get("s").getAsInt(),
                asJsonObject.get("g").getAsInt(),
                asJsonObject.get("esgRating").getAsInt(),
                asJsonObject.get("controversy").getAsInt());
    }

    JsonArray convertCollection(Map<EsgObject, Double> collection) {
        JsonArray array = new JsonArray();
        collection.entrySet().forEach(entry -> {
            JsonObject esgObject = new JsonObject();
            esgObject.addProperty("mfId", entry.getKey().getMfId());
            esgObject.addProperty("description", entry.getKey().getDescription());
            esgObject.addProperty("weight", entry.getValue());
            esgObject.addProperty("e", entry.getKey().getE());
            esgObject.addProperty("s", entry.getKey().getS());
            esgObject.addProperty("g", entry.getKey().getG());
            array.add(esgObject);
        });
        return array;
    }

    private void fillCollections(JsonArray factors, Map<EsgObject, Double> positiveWeighted, Map<EsgObject, Double> negativeWeighted) {
        for (JsonElement factor : factors) {
            JsonObject f = factor.getAsJsonObject();
            EsgObject esgObject = new EsgObject(
                    f.get("mfId").getAsString(),
                    f.get("description").getAsString(),
                    f.get("e").getAsInt(),
                    f.get("s").getAsInt(),
                    f.get("g").getAsInt());
            double weight = f.get("weight").getAsDouble();
            if (weight > 0) {
                positiveWeighted.put(esgObject, weight);
            } else {
                negativeWeighted.put(esgObject, weight);
            }
        }
    }
}
