package com.google.sps.data;

import java.io.Serializable;

/**
 * Class "User" encapsulates a Username, First name, and LastName
 */

 public class User implements Serializable {
     
    /**
     *
     */
    private static final long serialVersionUID = 3979553597067474530L;
    private String uName;
    private String fName;
    private String lName;

    public void setUName(String uName) {this.uName = uName;}
    public void setfName(String fName) {this.fName = fName;}
    public void setLName(String lName) {this.lName = lName;}

    public User(String uName, String fName, String lName){
        this.uName = uName;
        this.fName = fName;
        this.lName = lName;
    }

    public String getUName(){return this.uName;}
    public String getFName(){return this.fName;}
    public String getLName(){return this.lName;}
 }