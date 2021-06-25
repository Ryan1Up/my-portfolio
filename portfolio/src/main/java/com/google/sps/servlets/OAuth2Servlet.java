package com.google.sps.servlets;


import java.io.IOException;

import java.util.Collections;


import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.auth.oauth2.OAuth2Credentials;
import com.google.gson.Gson;
import com.google.sps.data.OAuth2ClientCredentials;

@WebServlet("/oauth2redirect")
public class OAuth2Servlet extends HttpServlet {

    /**
     * Specify App Name
     */
    protected String APP_NAME = "Ryan\'s Portfolio";

    /** Create Directory to store user Credentials */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"),
            ".store/portfolio");

    /** Create global instance of Datastorefactory to be shared across app */
    private static FileDataStoreFactory dataStoreFactory;

    /** Global instance of the HTTP transport. */
    private static HttpTransport httpTransport = new NetHttpTransport();

    /** Global instance of JSON factory, for reading/modifying json file/objects */
    private static final JsonFactory jsonFactory = new GsonFactory();

    private static final Gson gson = new Gson();

    
    /** Set URIs for Authorization Redirect, and token Retrieval */
    private static final String TOKEN_SERVER_URL = "https://oauth2.googleapis.com/token";
    private static final String AUTHORIZATION_SERVER_URL = "https://accounts.google.com/o/oauth2/auth";

    /** Authorizes the installed application to access user's protected data */
    private static Credential authorize() throws Exception {

        // Sets up authorization Flow
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
                httpTransport, jsonFactory, new GenericUrl(TOKEN_SERVER_URL),
                new ClientParametersAuthentication(OAuth2ClientCredentials.client_id, OAuth2ClientCredentials.client_secret),
                OAuth2ClientCredentials.client_id, AUTHORIZATION_SERVER_URL)
                        .setScopes(Collections.singleton(CalendarScopes.CALENDAR_EVENTS))
                        .setDataStoreFactory(dataStoreFactory).build();

        // Authorizes it
       LocalServerReceiver reciever = new LocalServerReceiver.Builder().setHost(OAuth2ClientCredentials.DOMAIN)
                .setPort(OAuth2ClientCredentials.PORT).build();
        
       
        return new AuthorizationCodeInstalledApp(flow, reciever).authorize("Ryan1Up");

    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) {
        try {
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
            Credential cred = authorize();
            

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) {
        try{
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
            final Credential credential = authorize();
            HttpRequestFactory requestFactory =
                httpTransport.createRequestFactory(
                    new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest request) throws IOException {
                        credential.initialize(request);
                        request.setParser(new JsonObjectParser(jsonFactory));
                }
              });
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    


    
}