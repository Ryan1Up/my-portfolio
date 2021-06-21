package com.google.sps.servlets;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
@WebServlet("/contact-storage-handler")
public class ContactInfoStorage extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // Return a Json containing all the contact requests on file
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //Get instance of Data store service
        Datastore dataStore = DatastoreOptions.getDefaultInstance().getService();

        //Build a Query for data, optional: OrderBy Timestamps
        Query<Entity> query = Query.newEntityQueryBuilder().setKind("Contact").setOrderBy(OrderBy.desc("timeStamp")).build();
        //Retrieve a list of Entities from the data store that match the query
        QueryResults<Entity> queryResults = dataStore.run(query);

        //A small helper class to help organize all the elements in a "Contact" Entity
        class Contact {
            private String name;
            private String email;
            private String msg;
            private long ID;

            Contact(String name, String email, String msg, long ID){
                this.name = name;
                this.email = email;
                this.msg = msg;
                this.ID = ID;
            }

            protected String getName(){
                return this.name;
            }
            protected String getEmail() {
                return this.email;
            }
            protected String getMsg(){
                return this.msg;
            }            
            protected long getID(){
                return this.ID;
            }
        }

        //Collection that will contain all the elements returned from Query
        List<Contact> contacts = new ArrayList<>();

        //Load all Query Results into Collection Container
        while(queryResults.hasNext()){
            Entity resultContact = queryResults.next();

            String name = resultContact.getString("name");
            String email = resultContact.getString("email");
            String msg = resultContact.getString("msg");
            long ID = resultContact.getKey().getId();

            Contact contact = new Contact(name, email, msg, ID);
           
            contacts.add(contact);
        }

        //Create a new Gson Instance to format return values
        Gson gson = new Gson();
        //Set return type to Json
        response.setContentType("application/json;");
        //Print Json object to response
        response.getWriter().println(gson.toJson(contacts));

    }
    //Stores the contact request made into permanent storage
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String msg = request.getParameter("msgId");

        /*Retrieve each of the parameters: name, email, and msgId, and clean them of HTML code */
        name = Jsoup.clean(name, Whitelist.none());
        email = Jsoup.clean(email, Whitelist.none());
        if(msg != null){
            msg = Jsoup.clean(request.getParameter("msgId"), Whitelist.none());
        }
        else {
            msg = "Default: No Message";
        }
        long timeStamp = System.currentTimeMillis(); //For ordering later
        //get a new instance of the data store service
        Datastore dataStore = DatastoreOptions.getDefaultInstance().getService();

        //Create a new KeyFactory for "Contact" elements
        KeyFactory keyFactory = dataStore.newKeyFactory().setKind("Contact");

        //Build new "Storage" entity to store in the NoSQL database
        FullEntity contactEntity = Entity.newBuilder(keyFactory.newKey())
            .set("name", name)
            .set("email", email)
            .set("msg", msg)
            .set("timeStamp", timeStamp)
            .build();

        dataStore.put(contactEntity);
        /**Using Java.time library to simplify Time outputs */
        String time = LocalTime.now( ZoneId.of( "America/Los_Angeles")).truncatedTo(ChronoUnit.SECONDS).toString();
        System.out.println("Name: " + name + 
                         "\nEmail: " + email +
                         "\nTimestamp: " + time +
                         "\nMessage: \n" + msg);
        
        response.sendRedirect("/contactThankYou.html");
    }


}