package com.google.sps.servlets;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.sps.util.DatastoreModule;
import com.google.sps.util.EmailComposer;
import com.google.sps.data.OAuth2Credentials;
import com.google.sps.util.CredentialManager;
import com.google.api.services.gmail.Gmail.Builder;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * Handles "Contact Me form" inputs and stores them in the servers ArrayList of
 * contact requests
 */
@WebServlet("/contact-storage-handler")
public class ContactServlet extends HttpServlet {

    private static final long serialVersionUID = -5444015804847619418L;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static Credential creds;
    private static Gmail service;
    private static final String CREDENTIALS_PATH = "client_secret.json";
    private static OAuth2Credentials APP_CREDENTIALS;

    @Override
    public void init() {
        DatastoreModule.init();
        try {
            setAppCredentials();
        } catch (final IOException e) {
            
            e.printStackTrace();
        }
    }

    private static void setAppCredentials() throws IOException {
        APP_CREDENTIALS = CredentialManager.setCredentials(CREDENTIALS_PATH);
    }

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse res) throws IOException {

        final String messages = DatastoreModule.getMessagesAsJson();

        res.setContentType("application/json;");
        res.getWriter().write(messages);
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        final String[] message = parseContactMessage(request);

        DatastoreModule.storeContactMessage(message);

        try {
            sendEmail(request, message);
        } catch (GeneralSecurityException | MessagingException e) {

            e.printStackTrace();
        }

        response.sendRedirect("/contactThankYou.html");
    }

    private static String[] parseContactMessage(final HttpServletRequest req) {
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String msg = req.getParameter("msgId");
        /*
         * Retrieve each of the parameters: name, email, and msgId, and clean them of
         * HTML code
         */
        name = Jsoup.clean(name, Whitelist.none());
        email = Jsoup.clean(email, Whitelist.none());
        if (msg != null) {
            msg = Jsoup.clean(req.getParameter("msgId"), Whitelist.none());
        } else {
            msg = "Default: No Message";
        }
        final String time = LocalTime.now(ZoneId.of("America/Los_Angeles")).truncatedTo(ChronoUnit.SECONDS).toString();
        final String[] message = { name, email, msg, time };

        return message;
    }

    private static void sendEmail(final HttpServletRequest request, final String[] message)
            throws GeneralSecurityException, IOException, AddressException, MessagingException {

        if (!DatastoreModule.isAuthorized()) {
            System.out.println("Error: Application is not authorized");
        }
        if (DatastoreModule.needsToBeRefreshed()) {
            DatastoreModule.refreshAccessToken(APP_CREDENTIALS);
        }

        final JsonObject credentialJson = new Gson().fromJson(DatastoreModule.getCredentials(), JsonObject.class);

        createCredentialsAndSendMessage(credentialJson, message);
    }

    /**
     * 
     * @param credentialJson
     * @param message
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws AddressException
     * @throws MessagingException
     * 
     *                                  Create the credential Object you need to
     *                                  send Emails Then send the message
     */
    private static void createCredentialsAndSendMessage(final JsonObject credentialJson, final String[] message)
            throws GeneralSecurityException, IOException, AddressException, MessagingException {

        buildCredentialsAndService(credentialJson);

        final MimeMessage mimeMessage = EmailComposer.createMessage(message[0], message[1] + "\n" + message[2]);

        EmailComposer.sendMessage(service, credentialJson.get("user_id").toString().replaceAll("\"", ""), mimeMessage);
    }

    private static void buildCredentialsAndService(final JsonObject credentialJson)
            throws GeneralSecurityException, IOException {

        if (creds == null || service == null) {

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            final TokenResponse tokenResponse = new TokenResponse();

            tokenResponse.setAccessToken(credentialJson.get("access_token").toString().replaceAll("\"", ""));
            // tokenResponse.setAccessToken(credentialJson.get("refresh_token").toString().replaceAll("\"",
            // ""));

            creds = createCredentialWithAccessTokenOnly(HTTP_TRANSPORT, JSON_FACTORY, tokenResponse);

            final Builder builder = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, creds);
            service = builder.setApplicationName(credentialJson.get("project_id").toString().replaceAll("\"", ""))
                    .build();
        }
    }

    // Build the Credential needed for Sending Email with GMail Service
    public static Credential createCredentialWithAccessTokenOnly(final HttpTransport transport,
            final JsonFactory jsonFactory, final TokenResponse tokenResponse) {

        return new Credential(BearerToken.authorizationHeaderAccessMethod()).setFromTokenResponse(tokenResponse);
    }

}