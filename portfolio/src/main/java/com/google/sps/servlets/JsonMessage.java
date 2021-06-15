package com.google.sps.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@WebServlet("/JsonMsg")
public class JsonMessage extends HttpServlet {

    private static List<String> servletContent = new ArrayList<>();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Convert the current server content to JSON
        String responseJson = convertToJsonTheHardWay();

        // Send the JSON as the response
        response.setContentType("application/json;");
        response.getWriter().println(responseJson);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Assumes input is formatted as "Key/Value, Key/Value,..." in the "input" field
        // of the request
        storeRequestInputs(request);
       
        response.sendRedirect(request.getContextPath() + "/jsonThing.html");
    }

    private static void storeRequestInputs(HttpServletRequest request) throws NullPointerException {
        String inputs = formatInputs(request);
        String[] textFieldInputs = inputs.split(",", 0);
        for (String input : textFieldInputs) {
            servletContent.add(input);
        }
    }

    public static String formatInputs(HttpServletRequest request) {
        String inputs = request.getParameter("inputs");
        System.out.println(inputs);
        inputs = inputs.replaceAll(" ", "");
        inputs = inputs.replaceAll("\r\n|\r|\n", ",");
        inputs = inputs.replaceAll(",,", ",");      
        
        
        System.out.println(inputs);
        return inputs;
}

    // Convert an ArrayList of strings to a Stringified JSON
    private String convertToJsonTheHardWay() {
        
        String[] keyValue = new String[2];
        String json = "{";
        String lastPair = servletContent.get(servletContent.size()-1);
        for(String pair : servletContent){
            keyValue = pair.split(":", 0);
            json += "\"" + keyValue[0] + "\"" + ": ";
            json += "\"" + keyValue[1] + "\"";
            if(!(pair == lastPair)){
                json += ", ";
            }
        }

        json += "}";
        System.out.println(json);
        return json;
    }

    private String convertToJsonUsingGson() {
    Gson gson = new Gson();
    String json = gson.toJson(servletContent);
    return json;
    }
}