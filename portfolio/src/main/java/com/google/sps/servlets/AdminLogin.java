package com.google.sps.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.services.gmail.GmailScopes;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.sps.data.OAuth2Credentials;
import com.google.sps.util.CredentialManager;

@WebServlet("/AdminLogin")
public class AdminLogin extends HttpServlet {


    /**
     * Serializeable UID
     */
    private static final long serialVersionUID = -3459795480308587743L;
    // File that contains the APP credentials
    private static final String APP_CREDENTIALS = "client_secret.json";
    private static OAuth2Credentials appCredentials;
    private static String AUTH_REQ_URL;
    protected static Datastore dataStore;
    private static final int TESTING = 0;
    private static final int PRODUCTION = 1;
    /**
     * init() initialized the authorization code flow to Authorize
     * "Ryan's Portfolio" to utilize the Gmail API to send Emails
     */
    @Override
    public void init()  {

        try{
            setAppCredentials();
            setDatastore();
            buildAuthUrl();

        }catch (final IOException e) {
            System.out.println("Credentials Not Loaded");
            e.printStackTrace();
        }
    }

    @Override
    public void service(final HttpServletRequest req, final HttpServletResponse res) throws IOException {
        checkLogin(req, res);
        res.sendRedirect(AUTH_REQ_URL);
    }

    private void checkLogin(final HttpServletRequest req, final HttpServletResponse res) throws IOException {
            if( req.getSession().getAttribute("access_token") != null)
                    res.sendRedirect(req.getContextPath()+ "/index.html");
            
    }

    private static void buildAuthUrl(){
        AUTH_REQ_URL = new StringBuilder()
                .append(appCredentials.getAuth_uri()).append("?")
                .append("&client_id=").append(appCredentials.getClient_id())
                .append("&response_type=code")
                .append("&redirect_uri=").append(appCredentials.getRedirect_uris()[TESTING])
                .append("&scope=").append(GmailScopes.GMAIL_SEND + " " + GmailScopes.GMAIL_COMPOSE + " https://www.googleapis.com/auth/userinfo.profile")
                .append("&state=adminLogin59")
                .append("&access_type=offline")
                .append("&approval_prompt=force")
                .toString();
    }

    private static void setAppCredentials() throws IOException {
        appCredentials = CredentialManager.setCredentials(APP_CREDENTIALS);
    }

    private static void setDatastore(){
        dataStore = DatastoreOptions.getDefaultInstance().getService();
    }
}