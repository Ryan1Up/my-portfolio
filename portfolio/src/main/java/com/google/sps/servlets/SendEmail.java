package com.google.sps.servlets;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class SendEmail {

    private static final int subjectIndex = 0;
    private static final int fromIndex = 1;
    private static final int hostIndex = 2;
    private static final int messageIndex = 3;

   public static void sendEmail(String[] content) {    
      // Recipient's email ID needs to be mentioned.
      String to = "anderson94@mail.fresnostate.edu";

      // Sender's email ID needs to be mentioned
      String from = content[fromIndex];

      // Assuming you are sending email from localhost
      String host = content[hostIndex];

      // I have to set the properties manually
      Properties properties = System.getProperties();

      // Setup mail server
      properties.setProperty("mail.smtp.host", host);

      // Get the default Session object.
      Session session = Session.getDefaultInstance(properties);

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