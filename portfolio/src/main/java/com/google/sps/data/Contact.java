package com.google.sps.data;

public class Contact {
    private String name;
    private String email;
    private String msg;
    private String ID;

    public Contact(String name, String email, String msg, String ID) {
        this.name = name;
        this.email = email;
        this.msg = msg;
        this.ID = ID;
    }

    protected String getName() {
        return this.name;
    }

    protected String getEmail() {
        return this.email;
    }

    protected String getMsg() {
        return this.msg;
    }

    protected String getID() {
        return this.ID;
    }
}
