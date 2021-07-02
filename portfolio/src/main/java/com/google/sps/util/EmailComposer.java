package com.google.sps.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;


import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import javax.mail.internet.MimeMessage;

/**
 * This is a class to simplify email compostion for use with the GMail API
 */
public class EmailComposer {

    private static final String from = "Anderson94@mail.fresnostate.edu";
    private static final String to = "Anderson94@mail.fresnostate.edu";

    public static MimeMessage createMessage(String subject, String body) throws AddressException, MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);
        email.setText(body);
        return email;
    }

    /**
     * Create a message from an email.
     *
     * @param emailContent Email to be set to raw of message
     * @return a message containing a base64url encoded email
     * @throws IOException
     * @throws MessagingException
     */
    public static Message createMessageWithEmail(MimeMessage emailContent)  throws MessagingException, IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    public static Message sendMessage(Gmail service, String userId, MimeMessage emailContent) throws MessagingException, IOException {

      
        Message message = createMessageWithEmail(emailContent);
        
        message = service.users().messages().send(userId, message).execute();
        
        return message;
    }
}