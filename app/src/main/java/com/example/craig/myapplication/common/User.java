package com.example.craig.myapplication.common;

import com.example.craig.myapplication.util.MD5;

import java.util.ArrayList;

public class User
        extends FirebaseItem {
    public User() {
        this.listIds = new ArrayList<>();
    }

    public ArrayList<String> getListIds() {
        return listIds;
    }

    public void setListIds(ArrayList<String> listIds) {
        this.listIds = listIds;
    }

    public void addList(String listUid) {
        this.listIds.add(listUid);
    }

    public void removeList(String listUid) {
        this.listIds.remove(listUid);
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static String createUid(String email) {
        return MD5.hash(email);
    }

    private ArrayList<String> listIds;
    private String photoUrl;
    private String email;
}
