package com.google.sps.servlets;

import java.util.*;


import javax.mail.*;
import javax.mail.internet.*;


public class SendEmail {

    

    public static void sendEmail(String name, String email, String emailContent) {
        // Recipient's email ID needs to be mentioned.
        String myAccountEmail = "anderson94.rpa@gmail.com";
        String password = "";


        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.socketFactory.port", "587");    
        props.put("mail.smtp.socketFactory.class",    
                  "javax.net.ssl.SSLSocketFactory"); 

        // get Session
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myAccountEmail, password);
            }
        });

         try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myAccountEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(myAccountEmail));
            message.setSubject(name);
            message.setText(email + "\n" + emailContent);
            System.out.println("Message Compiled, getting ready for transport...");
            Transport.send(message);
            System.out.println("Message Sent Successfully");
        } catch (Exception ex) {
            //Logger.getLogger(SendEmail.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            System.out.println("Message Not Sent");
        }
        
    }

    
}