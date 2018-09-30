package com.example.craig.myapplication.common;

public abstract class FirebaseItem {

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    private String uid;
}
