package com.google.sps.servlets;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class SendEmail {

    private static final int subjectIndex = 0;
    private static final int fromIndex = 1;
    private static final int messageIndex = 2;

   public static void sendEmail(String[] content) {    
      // Recipient's email ID needs to be mentioned.
      String to = "anderson94@mail.fresnostate.edu";

      // Sender's email ID needs to be mentioned
      String from = content[fromIndex];


      Properties props = new Properties();  
          props.put("mail.smtp.user", to);  
          props.put("mail.smtp.host", "smtp.gmail.com");
          props.put("mail.smtp.starttls.enable","true");    
          props.put("mail.smtp.socketFactory.port", "465");    
          props.put("mail.smtp.socketFactory.class",    
                    "javax.net.ssl.SSLSocketFactory");    
          props.put("mail.smtp.auth", "true");    
          props.put("mail.smtp.port", "465");    


          //get Session   
          Session session = Session.getDefaultInstance(props,    
           new javax.mail.Authenticator() {    
           protected PasswordAuthentication getPasswordAuthentication() {    
           return new PasswordAuthentication(to,"H2vhf794!");  
           }    
          });    

      try {
         // Create a default MimeMessage object.
         MimeMessage message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(from));

         // Set To: header field of the header.
         message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

         // Set Subject: header field
         message.setSubject("Contact from Portfolio: " + content[subjectIndex]);

         // Now set the actual message
         message.setText(content[messageIndex]);

         // Send message
         Transport.send(message);
         System.out.println("Sent message successfully....");
      } catch (MessagingException mex) {
         mex.printStackTrace();
      }
   }
}