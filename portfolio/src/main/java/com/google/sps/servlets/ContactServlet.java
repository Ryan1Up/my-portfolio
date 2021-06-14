package com.google.sps.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.net.UnknownHostException;

/** Handles requests sent to the /hello URL. Try running a server and navigating to /hello! */
@WebServlet("/contact-handler")
public class ContactServlet extends HttpServlet {

    
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    // Get the value entered in the form.
    String textValue = request.getParameter("name");
    String emailValue = request.getParameter("emailId");
    
    String message = request.getParameter("msgId");
    
    HttpSession ses = request.getSession();
    System.out.println(ses.getAttribute("contacted"));
    ses.setAttribute("contacted", "true");
    // Print the value so you can see it in the server logs.
    System.out.println("You submitted: " + textValue);
    System.out.println("You submitted: " + emailValue);
    System.out.println("You submitted: "+ message);
    InetAddress ip;
    try {
        ip = InetAddress.getLocalHost();
        String ipS = ip.toString();
        ipS = ipS.split("/", 0)[1];
        System.out.println(ipS);
        String[] emailContents = {textValue, emailValue, ipS, message};
        SendEmail.sendEmail(emailContents);
       
    } catch (UnknownHostException e) {
        e.printStackTrace();
    }
    
    response.sendRedirect(request.getContextPath() + "/contactThankYou.html");
    

  }

  
}