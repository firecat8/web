/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.reader;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import com.google.gson.stream.JsonReader;
import java.io.InputStreamReader;

/**
 *
 * @author Plamen
 */
public class JsonFileReader<T> {

    // to load a json string into a custom object need to make a type token of the data;
    private Type type;

    public JsonFileReader(Type type) {
        this.type = type;

    }

    public T readJsonFile(String filename) {
        JsonReader reader = new JsonReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
       return new Gson().fromJson(reader, type);

    }

}
