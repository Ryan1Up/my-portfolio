package com.google.sps.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.sps.data.OAuth2Credentials;
import com.google.sps.util.CredentialManager;
import com.google.sps.util.DatastoreModule;
import org.apache.http.ParseException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

@WebServlet("/oAuth2Callback")
public class AdminLoginCallback extends HttpServlet {
    
   
    private static final long serialVersionUID = -7449922251630038432L;
    private static final String CREDENTIALS_PATH = "client_secret.json";
    private static OAuth2Credentials APP_CREDENTIALS;
    private static String TOKEN_REQ_URL;
    private static String USER_INFO_URL = "https://www.googleapis.com/oauth2/v1/userinfo?&access_token=";
   // private static final int TESTING = 0;
    private static final int PRODUCTION = 1;
    private static CloseableHttpClient HTTP_CLIENT;

   
    @Override
    public void init() {
        try {
            setAppCredentials();
            connectToDatastore();
            initHttpClient();
        } catch (IOException e) {
            System.out.println("Error Loading Credentials");
            e.printStackTrace();
        }
    }

    private static void setAppCredentials() throws IOException {
        APP_CREDENTIALS = CredentialManager.setCredentials(CREDENTIALS_PATH);
    }

    private static void connectToDatastore() {
        DatastoreModule.init();
    }

    private static void initHttpClient() {
        HTTP_CLIENT = HttpClientBuilder.create().build();
    }


     @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {


        // If access denied, go to homepage
       checkResponseForError(req, res);

        /**
         * On successful authorization request: Use returned code to request
         * access_token, etc Use access_token to request basic user information Use
         * profile id to check if user is already in database if not add them to
         * database set session attribute for user id return to main user content page
         */
        JsonObject userCredentials = requestToken(req, res);
       
        String userAccess_token = userCredentials.get("access_token").toString().replaceAll("\"", "");
        JsonObject userInfo = requestUserInfo(userAccess_token);
    
        String userId = userInfo.get("id").toString().replaceAll("\"", "");

        /**
         * Datastore Service Credentials needed fields:
         *      access_token
         *      refresh_token
         *      expiration
         *      profile_id
         *      client_id
         *      client_secret
         */
        
         /**
          * Upon retrieving all the necessary information, store it
          */
        
             DatastoreModule.storeUserCredentials(userCredentials, userId);
             System.out.println("Storing Creds");
         if(!DatastoreModule.isAuthorized()){
             System.out.println("Not Authorized from Line 100");
         }

         res.sendRedirect(req.getContextPath() + "/index.html");

    }

    private static JsonObject requestUserInfo(String access_token) throws ClientProtocolException, IOException {
        String userInfoRequest = USER_INFO_URL + access_token;

        HttpGet request = new HttpGet(userInfoRequest);

        HttpResponse response = HTTP_CLIENT.execute(request);

        JsonObject userInfo = extractJsonFromResponse(response);

        return userInfo;
    }

    private void checkResponseForError(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (req.getParameter("error") != null || !(req.getParameter("state").toString().equals("adminLogin59"))) {
            System.out.print("Error from Response: ");
            if (req.getParameter("error") != null)
                System.out.println("user denied access");
            else
                System.out.println("Invalid State, possible attack on request traffic");

            res.sendRedirect(req.getContextPath() + "/index.html");
            return;
        }
        return;
    }

    private static JsonObject requestToken(HttpServletRequest req, HttpServletResponse res)
            throws ClientProtocolException, IOException {

        HttpResponse response = sendTokenRequest(req.getParameter("code"));
        JsonObject userCredentials = extractJsonFromResponse(response);

        return userCredentials;

    }

    private static HttpResponse sendTokenRequest(String code) throws ClientProtocolException, IOException {

        buildTokenUri(code);
        
        HttpPost request = new HttpPost(TOKEN_REQ_URL);

        return HTTP_CLIENT.execute(request);
    }

    private static void buildTokenUri(String code){

        StringBuilder sb = new StringBuilder().append(APP_CREDENTIALS.getToken_uri()).append("?").append("&client_id=")
                .append(APP_CREDENTIALS.getClient_id()).append("&client_secret=")
                .append(APP_CREDENTIALS.getClient_secret()).append("&code=").append(code)
                .append("&grant_type=authorization_code").append("&redirect_uri=")
                .append(APP_CREDENTIALS.getRedirect_uris()[PRODUCTION]);
        TOKEN_REQ_URL = sb.toString();        
    }

    private static JsonObject extractJsonFromResponse(HttpResponse response) throws ParseException, IOException {
        HttpEntity entity = response.getEntity();
        // Stringify the entity into a Json String
        String body = EntityUtils.toString(entity);
       

        // turn Stringified JSON into JSON object
        JsonObject returnJson = new Gson().fromJson(body, JsonObject.class);

        return returnJson;
    }
}