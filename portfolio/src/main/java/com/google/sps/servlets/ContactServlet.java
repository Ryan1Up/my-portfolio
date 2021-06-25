package com.google.sps.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

/** Handles "Contact Me form" inputs and stores them in the servers ArrayList of contact requests */
@WebServlet("/contact-handler")
public class ContactServlet extends HttpServlet {

    private static ArrayList<String> contactRequests = new ArrayList<>();
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String[] contactRequestInfo = new String[3];
        parseContactRequest(request, contactRequestInfo);

        saveRequest(contactRequestInfo);

        String requestInfo = "";
        requestInfo += "Name: " + contactRequestInfo[0];
        requestInfo += "\nEmail: " + contactRequestInfo[1];
        requestInfo += "\nMessage: " + contactRequestInfo[2];

        System.out.println(requestInfo);

        response.sendRedirect(request.getContextPath() + "/contactThankYou.html");
    }

    private static void parseContactRequest(HttpServletRequest request, String[] requestInfo) {
        // Assumes 3 Input Fields Fromt the Request Field
        // Assumes a Name Field, Email Field, and Message Field
        requestInfo[0] = request.getParameter("name");
        requestInfo[1] = request.getParameter("emailId");
        String msg = request.getParameter("msgId");
        if( msg == null) msg = "No Message";
        requestInfo[2] = msg;        
    }

    private void saveRequest(String[] contactRequestInfo) throws NullPointerException {
        String requestInfo = "";
        requestInfo += "Name: " + contactRequestInfo[0];
        requestInfo += "\nEmail: " + contactRequestInfo[1];
        requestInfo += "\nMessage: " + contactRequestInfo[2];
        contactRequests.add(requestInfo);      
   }
}