package com.google.sps.servlets;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.Gmail.Builder;
import com.google.api.services.gmail.model.Message;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.gson.Gson;
import com.google.sps.data.Contact;
import com.google.sps.util.EmailComposer;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;

@WebServlet("/contact-storage-handler")
public class ContactInfoStorage extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    // Return a Json containing all the contact requests on file
    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        // Get instance of Data store service
        final Datastore dataStore = DatastoreOptions.getDefaultInstance().getService();

        // Build a Query for data, optional: OrderBy Timestamps
        final Query<Entity> query = Query.newEntityQueryBuilder().setKind("Contact")
                .setOrderBy(OrderBy.desc("timeStamp")).build();
        // Retrieve a list of Entities from the data store that match the query
        final QueryResults<Entity> queryResults = dataStore.run(query);

        // A small helper class to help organize all the elements in a "Contact" Entity

        // Collection that will contain all the elements returned from Query
        final List<Contact> contacts = new ArrayList<>();
        final Gson gson = new Gson();

        // Load all Query Results into Collection Container
        while (queryResults.hasNext()) {
            final Entity resultContact = queryResults.next();

            final String name = resultContact.getString("name");
            final String email = resultContact.getString("email");
            final String msg = resultContact.getString("msg");
            final long ID = resultContact.getKey().getId();

            final Contact contact = new Contact(name, email, msg, ID);
            // String jsonString = "{ \"Name\":" + name + "}"
            contacts.add(contact);
        }

        // Create a new Gson Instance to format return values

        // Set return type to Json
        response.setContentType("application/json;");
        // Print Json object to response
        response.getWriter().println(gson.toJson(contacts));

    }

    // Stores the contact request made into permanent storage
    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String msg = request.getParameter("msgId");

        /*
         * Retrieve each of the parameters: name, email, and msgId, and clean them of
         * HTML code
         */
        name = Jsoup.clean(name, Whitelist.none());
        email = Jsoup.clean(email, Whitelist.none());
        if (msg != null) {
            msg = Jsoup.clean(request.getParameter("msgId"), Whitelist.none());
        } else {
            msg = "Default: No Message";
        }
        final String time = LocalTime.now(ZoneId.of("America/Los_Angeles")).truncatedTo(ChronoUnit.SECONDS).toString();

        // Send Message Here
        if (request.getSession().getAttribute("userId") != null) {
            try {

                final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                final TokenResponse tokenResponse = new TokenResponse();
                tokenResponse.setAccessToken(
                        request.getSession().getAttribute("access_token").toString().replaceAll("\"", ""));
                tokenResponse.set("project_id", "randerson-sps-summer21");
                final Credential creds = createCredentialWithAccessTokenOnly(HTTP_TRANSPORT, JSON_FACTORY,
                        tokenResponse);

               

                final MimeMessage mimeMessage = EmailComposer.createMessage(name, email + "\n" + msg);
                
                Builder builder = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, creds);
                Gmail service = builder.setApplicationName("randerson-sps-summer21").build();
                final Message sendMessage = EmailComposer.sendMessage(service,
                        request.getSession().getAttribute("userId").toString().replaceAll("\"", ""), mimeMessage);
                
            } catch (final MessagingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final GeneralSecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // get a new instance of the data store service
        final Datastore dataStore = DatastoreOptions.getDefaultInstance().getService();

        // Create a new KeyFactory for "Contact" elements
        final KeyFactory keyFactory = dataStore.newKeyFactory().setKind("Contact");

        // Build new "Storage" entity to store in the NoSQL database
        final FullEntity contactEntity = Entity.newBuilder(keyFactory.newKey()).set("name", name).set("email", email)
                .set("msg", msg).set("timeStamp", time).build();

        dataStore.put(contactEntity);
        /** Using Java.time library to simplify Time outputs */

        System.out.println("Name: " + name + "\nEmail: " + email + "\nTimestamp: " + time + "\nMessage: \n" + msg);

        response.sendRedirect("/contactThankYou.html");
    }

    public static Credential createCredentialWithAccessTokenOnly(final HttpTransport transport,
            final JsonFactory jsonFactory, final TokenResponse tokenResponse) {
                
    return new Credential(BearerToken.authorizationHeaderAccessMethod()).setFromTokenResponse(
        tokenResponse);
  }

}