package com.google.sps.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.KeyFactory;

import com.google.cloud.datastore.Key;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.sps.data.Contact;
import com.google.sps.data.OAuth2Credentials;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.cloud.datastore.Query;

import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.OrderBy;

public class DatastoreModule {

    private static Datastore dataStore;
    private static final String quotes = "\"";
    public static final String email = "Anderson94@mail.fresnostate.edu";
    private static KeyFactory keyFactory;
    private static String REFRESH_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static CloseableHttpClient HTTP_CLIENT;

    public static void init() {
        if (dataStore == null) {
            dataStore = DatastoreOptions.getDefaultInstance().getService();
            keyFactory = dataStore.newKeyFactory();
            HTTP_CLIENT = HttpClientBuilder.create().build();
        }

    }

    public static void storeContactMessage(String[] messageContents) {

        KeyFactory messageKey = dataStore.newKeyFactory().setKind("Messages");

        Entity messageEntity = Entity.newBuilder(messageKey.newKey(messageContents[3])).set("From", messageContents[0])
                .set("Email", messageContents[1]).set("Message", messageContents[2])
                .set("Timestamp", messageContents[3]).build();
        dataStore.put(messageEntity);

    }

    public static QueryResults<Entity> getMessages() {
        // Build a Query for data, optional: OrderBy Timestamps
        final Query<Entity> query = Query.newEntityQueryBuilder().setKind("Messages")
                .setOrderBy(OrderBy.desc("Timestamp")).build();
        // Retrieve a list of Entities from the data store that match the query
        final QueryResults<Entity> queryResults = dataStore.run(query);

        return queryResults;
    }

    public static String getMessagesAsJson() {
        QueryResults<Entity> queryResults = getMessages();

        final List<Contact> messages = new ArrayList<>();
        final Gson gson = new Gson();

        // Load all Query Results into Collection Container
        while (queryResults.hasNext()) {
            final Entity resultContact = queryResults.next();

            final String name = resultContact.getString("From");
            final String email = resultContact.getString("Email");
            final String msg = resultContact.getString("Message");
            final String ID = resultContact.getString("Timestamp");

            final Contact message = new Contact(name, email, msg, ID.toString());
            messages.add(message);
        }

        return gson.toJson(messages);

    }

    public static void storeUserCredentials(JsonObject userCredentials, String userId) {

        /**
         * userCredential contains: { "access_token": "access_token" "refresh_token":
         * "refresh_token" "scope": "scopes" "token_type": "token_type" "expires_in":
         * "expiration in seconds" }
         */

        keyFactory.setKind("UserCredentials");
        long timeStampInSeconds = System.currentTimeMillis() / 1000;

        Entity credentialEntity = Entity.newBuilder(keyFactory.newKey(DatastoreModule.email))
                .set("timestamp", timeStampInSeconds).set("id", userId)
                .set("access_token", userCredentials.get("access_token").toString().replaceAll(quotes, ""))
                .set("refresh_token", userCredentials.get("refresh_token").toString().replaceAll(quotes, ""))
                .set("scope", userCredentials.get("scope").toString().replaceAll(quotes, ""))
                .set("token_type", userCredentials.get("token_type").toString().replaceAll(quotes, ""))
                .set("expires_in", userCredentials.get("expires_in").toString().replaceAll(quotes, ""))
                .set("id_token", userCredentials.get("id_token").toString().replaceAll(quotes, "")).build();

        dataStore.add(credentialEntity);
    }

    public static Boolean isAuthorized() {

        try {
            keyFactory.setKind("UserCredentials");
            Key credentialKey = keyFactory.newKey(DatastoreModule.email);
            String st = dataStore.get(credentialKey).getString("id");
            if (st != "" && st != null)
                return true;
        } catch (NullPointerException e) {

            return false;
        } catch (com.google.cloud.datastore.DatastoreException e) {

            return false;
        }

        return true;
    }

    public static Boolean needsToBeRefreshed() {
        // Get the Credentials
        keyFactory.setKind("UserCredentials");
        Key credentialKey = keyFactory.newKey(DatastoreModule.email);
        Entity credentials = dataStore.get(credentialKey);

        // Get Creds Expiration (in seconds)
        long expires_in = Long.parseLong(credentials.getString("expires_in"));
        // Get time stamp
        long credentialTimeStamp = credentials.getLong("timestamp");
        // If difference between current time and the time stamp is greater than
        // expiration time, return true
        return ((System.currentTimeMillis() / 1000) - credentialTimeStamp) > expires_in;
    }

    public static void refreshAccessToken(OAuth2Credentials appCredentials) throws ClientProtocolException, IOException {
        /**
         *  client_id=8819981768.apps.googleusercontent.com&
            client_secret={client_secret}&
            refresh_token=1/6BMfW9j53gdGImsiyUH5kU5RsR4zwI9lUVX-tqf8JXQ&
            grant_type=refresh_token

            https://accounts.google.com/o/oauth2/token refresh_token=1/nJZGF7hIySVtVCl8I-Y3KfXAPk84gD0X6ym7hQS8gcc client_id=XXXX client_secret=XXXX grant_type=refresh_token
         */
       
        //Get the Credentials
        keyFactory.setKind("UserCredentials");
        Key credentialKey = keyFactory.newKey(DatastoreModule.email);
        Entity credentials = dataStore.get(credentialKey);

        //Build Request for new token based on the refresh token url
        StringBuilder sb = new StringBuilder().append(REFRESH_TOKEN_URL).append("?")
                                              .append("&client_id=").append(appCredentials.getClient_id())
                                              .append("&client_secret=").append(appCredentials.getClient_secret())
                                              .append("&refresh_token=").append(credentials.getString("refresh_token").replaceAll("\"",""))
                                              .append("&grant_type=refresh_token");
        String token_req_url = sb.toString();

        //Build Request
        HttpPost request = new HttpPost(token_req_url);

        //Execute the request
        HttpResponse response = HTTP_CLIENT.execute(request);

        //Get response entity
        HttpEntity entity = response.getEntity();

        // Stringify the entity into a Json String
        String body = EntityUtils.toString(entity);
       
        // turn Stringified JSON into JSON object
        JsonObject returnJson = new Gson().fromJson(body, JsonObject.class);

        //Extract Access Token
        String newAccess_Token = returnJson.get("access_token").toString().replaceAll("\"", "");

        //Update the datastore entity
        Entity newCreds = Entity.newBuilder(dataStore.get(credentialKey)).set("access_token", newAccess_Token).build();

        //Store the update entity
        dataStore.update(newCreds);

    }

    public static String getCredentials(){
        keyFactory.setKind("UserCredentials");
        Key credentialKey = keyFactory.newKey(DatastoreModule.email);
        Entity credentials = dataStore.get(credentialKey);
        StringBuilder sb = new StringBuilder()
        .append("{")
                .append("\n\"access_token\":").append("\"" + credentials.getString("access_token") + "\"")
                .append(",\n\"refresh_token\":").append("\"" + credentials.getString("refresh_token") + "\"")
                .append(",\n\"project_id\":\"randerson-sps-summer21\"")
                .append(",\n\"user_id\":").append("\"" + credentials.getString("id") + "\"")
        .append("\n}");
                
        return sb.toString();
    }
}