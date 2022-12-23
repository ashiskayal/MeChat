package com.kayalprints.mechat.classes;

public class PersonalMassage {
    private String massage, sender;

    public PersonalMassage() {}

    public PersonalMassage(String massage, String sender) {
        this.massage = massage;
        this.sender = sender;
    }

    public void setMassage(String massage) {
        this.massage = massage;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMassage() {
        return massage;
    }

    public String getSender() {
        return sender;
    }
}
