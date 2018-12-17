/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.servlet;

import com.ers.v1.reader.JsonFileReader;
import com.ers.v1.user.User;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Plamen
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    private final JsonFileReader<List<User>> JSON_FILE_READER = new JsonFileReader<>(new TypeToken<List<User>>() {
    }.getType());

    private final static String INDEX_URL = "/inea/index.jsp";
    private final static String LOGIN_URL = "/inea/login.jsp";

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
        response.setContentType("text/html;charset=UTF-8");
        final String username = request.getParameter("username");
        final String password = request.getParameter("password");

        if (areCredentialsValid(username, password)) {
            setSessionAttributes(request.getSession(true), username, password);

            response.sendRedirect(INDEX_URL);
        } else {
            response.sendRedirect(LOGIN_URL + "?invalidLogin=true");
        }

    }

    private void setSessionAttributes(HttpSession session, String username, String password) {
        session.setAttribute("username", username);
        session.setAttribute("password", password);
    }

    private boolean areCredentialsValid(final String username, final String password) {
        User userFromRequest = new User(username, password);
        List<User> users = (List<User>) JSON_FILE_READER.readJsonFile("/users.json");
        return users.stream().anyMatch((user) -> (Objects.equals(user, userFromRequest)));
    }

}
