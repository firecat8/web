/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ers.v1.reader;

import com.ers.v1.user.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *??????????????????????
 * @author Plamen
 */
public class JsonFileReaderTestCase {

    private List<User> users;
    // to load a json string into a custom object need to make a type token of the data;
    private final Type userType = new TypeToken<List<User>>() {
    }.getType();
    private static final String FILEPATH = "src/main/resources/users.json";

    @Before
    public void setUp() {

        users = new ArrayList<>();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void readTest() {

        Gson gson = new Gson();
        //  JsonReader reader = new JsonReader(new InputStreamReader(getClass().getResourceAsStream("/config.txt"))));
        JsonReader reader = new JsonReader(new InputStreamReader(getClass().getResourceAsStream("/users.json")));
        users = gson.fromJson(reader, userType);
        User user = users.get(0);
        String actual = user.getUsername();
        String expected = "user";

        assertEquals(expected, actual);
    }

    @Test
    public void areCredentialsValid() {
        User userFromRequest = new User("admin", "admin");
        readTest();
        boolean actual = users.stream().anyMatch((user) -> (Objects.equals(user, userFromRequest)));
        assertTrue(actual);
    }

}
