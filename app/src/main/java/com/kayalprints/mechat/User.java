package com.kayalprints.mechat;

public class User {

    private String userName, dpLink;

    public User(String userName, String dpLink) {
        this.userName = userName;
        this.dpLink = dpLink;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDpLink() {
        return dpLink;
    }

    public void setDpLink(String dpLink) {
        this.dpLink = dpLink;
    }
}
