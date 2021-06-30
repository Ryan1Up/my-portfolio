package com.google.sps.servlets;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.sps.data.OAuth2Credentials;
import com.google.sps.util.CredentialManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

@WebServlet("/oAuth2Callback")
public class AdminCallBack extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = -1621776447195779456L;

    // File that contains the APP credentials
    private static final String APP_CREDENTIALS = "client_secret.json";
    private static OAuth2Credentials appCredentials;
    protected static Datastore dataStore;
    private static String TOKEN_REQ_URL;
    private static String USER_INFO_URL = "https://www.googleapis.com/oauth2/v1/userinfo?&access_token=";
    private static final int TEST_PROD = 1;
    private static HttpClientBuilder builder;
    private static CloseableHttpClient httpClient;

    @Override
    public void init() {

        try {
            appCredentials = CredentialManager.setCredentials(APP_CREDENTIALS);
            dataStore = DatastoreOptions.getDefaultInstance().getService();
            builder = HttpClientBuilder.create();
            httpClient = builder.build();

        } catch (IOException e) {
            System.out.println("Credentials Not Loaded");
            e.printStackTrace();
        }
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {

        // If access denied, go to homepage
        onError(req, res);
        try {
            onSuccess(req, res);
        } catch (GeneralSecurityException e) {

            e.printStackTrace();
        }
    }

    private void onError(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (req.getParameter("error") != null || !(req.getParameter("state").toString().equals("adminLogin59"))) {
            res.sendRedirect(req.getContextPath() + "/index.html");
            return;
        }
        return;
    }

    private void onSuccess(HttpServletRequest req, HttpServletResponse res)
            throws IOException, GeneralSecurityException {

        /**
         * onSuccess -> createAndStoreCredentials, redirect to welcome
         */
        createAndStoreCredentials(req);
        res.sendRedirect(req.getContextPath() + "/adminLogin.html");

    }

    private static void createAndStoreCredentials(HttpServletRequest req) throws ClientProtocolException, IOException {

        JsonObject newCredentials = createCredentials(req);

        storeCredentials(newCredentials);
    }

    private static JsonObject createCredentials(HttpServletRequest req) throws ClientProtocolException, IOException {
        /**
         * to create credentials: build url and send token request Extract "JsonObject
         * from response to request"
         */

        String codeForAccessToken = req.getParameter("code");
        HttpResponse response = sendTokenRequest(codeForAccessToken);
        JsonObject userCredentials = extractJsonFromResponse(response);

        return userCredentials;

    }

    private static HttpResponse sendTokenRequest(String codeForAccessToken)
            throws ClientProtocolException, IOException {

        /**
         * sendTokenRequest: build the request URL Format into an HttpRequest Execute
         * the Request
         */
        // Build the request URL
        buildTokenUrl(codeForAccessToken);
        // Create an HTML Post Request to the created URI
        HttpPost request = new HttpPost(TOKEN_REQ_URL);
        // Execute the Request
        HttpResponse response = httpClient.execute(request);

        return response;

    }

    /**
     * @param code
     * 
     *             Build the HTTP request URL for an access token Request
     *             parameters: Token_uri Client_id Client_secret Auth_Code (code)
     *             Redirect Uri
     */
    private static void buildTokenUrl(String codeForAccessToken) {

        TOKEN_REQ_URL = new StringBuilder().append(appCredentials.getToken_uri()).append("?").append("&client_id=")
                .append(appCredentials.getClient_id()).append("&client_secret=")
                .append(appCredentials.getClient_secret()).append("&code=").append(codeForAccessToken)
                .append("&grant_type=authorization_code").append("&redirect_uri=")
                .append(appCredentials.getRedirect_uris()[TEST_PROD]).toString();

    }

    private static JsonObject extractJsonFromResponse(HttpResponse response) throws ParseException, IOException {

        // Retrieve the "entity" attached to the response, in this case its essentailly
        // a JSON
        HttpEntity entity = response.getEntity();
        // Stringify the entity into a Json String
        String body = EntityUtils.toString(entity);
        System.out.println(body);

        // turn Stringified JSON into JSON object
        JsonObject returnJson = new Gson().fromJson(body, JsonObject.class);

        return returnJson;

    }

    private static void storeCredentials(JsonObject userCredentials) throws ClientProtocolException, IOException {
        /**
         * userCredentials contain the current access_token, refresh_token, etc
         * 
         * Step 1: Retrieve their user info profile Step 2: Check if they are in
         * database by checking against "profile_id" Step 3: if not, add them to the
         * database Step 4: Store user credentials into database, with additional
         * "profile_id" field (allows tracking later)
         */

        String access_token = userCredentials.get("access_token").toString().replaceAll("\"", "");
        JsonObject userInfo = getUserInfo(access_token);

        String profileId = userInfo.get("id").toString().replaceAll("\"", "");
        KeyFactory keyFactory = dataStore.newKeyFactory().setKind("UserProfile");
        Key userIdKey = keyFactory.newKey(profileId);
        if (!isUserInDatastore(userIdKey)) {
            addUserToDataStore(userInfo);
            addCredentialsToDatastore(userCredentials, profileId);
        } else
            return;

    }

    private static JsonObject getUserInfo(String access_token) throws ClientProtocolException, IOException {
        /**
         * Steps to retrive user info: Use access token to make a request to googleapi
         * for user information Extract and return json from response
         */

        HttpResponse response = sendUserInfoRequest(access_token);
        JsonObject userInfo = extractJsonFromResponse(response);

        return userInfo;

    }

    private static HttpResponse sendUserInfoRequest(String access_token) throws ClientProtocolException, IOException {

        // Create an HTML Get Request to the created URI
        HttpGet request = new HttpGet(USER_INFO_URL + access_token);
        // Execute the Request
        HttpResponse response = httpClient.execute(request);

        return response;
    }

    private static boolean isUserInDatastore(Key userId){
     
        return (dataStore.get(userId) != null);
    }


    private static void addUserToDataStore(JsonObject userInfo){

        /**
         * userInfo json contains the following:
         * 
        *  "id": "113591704021226668726",
           "name": "Ryan Anderson",
           "given_name": "Ryan",
           "family_name": "Anderson",
           "picture": "https://lh3.googleusercontent.com/a/AATXAJwV1nPAUtdTZr-7qwfxMt6f1Ih9GJZ2Y-SR4cWt=s96-c",
            "locale": "en"

            Storing data in dataStore:
                Create key factory
                Create Entity with all required fields
                Add entity to the store

            Note: Elements from Json objects may get returned with Quotes "", scrub them of the quotes if needed
        */

        KeyFactory keyFactory = dataStore.newKeyFactory().setKind("UserProfile");

        FullEntity userEntity = Entity.newBuilder(keyFactory.newKey(userInfo.get("id").toString().replaceAll("\"","")))
                                      .set("id", userInfo.get("id").toString().replaceAll("\"",""))
                                      .set("name", userInfo.get("name").toString().replaceAll("\"",""))
                                      .set("given_name", userInfo.get("given_name").toString().replaceAll("\"",""))
                                      .set("family_name", userInfo.get("family_name").toString().replaceAll("\"",""))
                                      .set("picture", userInfo.get("picture").toString().replaceAll("\"",""))
                                      .build();
        dataStore.add(userEntity);
        return;


    }


    private static void addCredentialsToDatastore(JsonObject userCredentials, String profileId){

        /**
         * userCredential contains:
         * {
         *  "access_token": "access_token"
         *  "refresh_token": "refresh_token"
         *  "scope": "scopes"
         *  "token_type": "token_type"
         *  "expires_in": "expiration in seconds"
         * }
         */
        KeyFactory keyFactory = dataStore.newKeyFactory().setKind("UserCredentials");
        long timeStampInSeconds = System.currentTimeMillis() /1000;

        FullEntity credentialEntity = Entity.newBuilder(keyFactory.newKey(profileId))
                .set("timestamp", timeStampInSeconds)
                .set("id", profileId)
                .set("access_token", userCredentials.get("access_token").toString().replaceAll("\"",""))
                .set("refresh_token", userCredentials.get("refresh_token").toString().replaceAll("\"",""))
                .set("scope", userCredentials.get("scope").toString().replaceAll("\"",""))
                .set("token_type", userCredentials.get("token_type").toString().replaceAll("\"",""))
                .set("expires_in", userCredentials.get("expires_in").toString().replaceAll("\"",""))
                .set("id_token", userCredentials.get("id_token").toString().replaceAll("\"",""))
                .build();

        dataStore.add(credentialEntity);
    }
    
  
}

