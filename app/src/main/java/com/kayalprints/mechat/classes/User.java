package com.kayalprints.mechat.classes;

public class User {

    private String userName, dpLink, phNumber;

    public User(String userName, String dpLink, String phNumber) {
        this.userName = userName;
        this.dpLink = dpLink;
        this.phNumber = phNumber;
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

    public String getPhNumber() {
        return phNumber;
    }

    public void setPhNumber(String phNumber) {
        this.phNumber = phNumber;
    }
}
