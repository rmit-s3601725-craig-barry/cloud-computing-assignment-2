package com.example.craig.myapplication.common;

import java.util.ArrayList;

public class CollabList
        extends FirebaseItem {

    public CollabList() {
        this.participants = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    public CollabList(String listName, String owner) {
        this.listName = listName;
        this.participants = new ArrayList<>();
        this.items = new ArrayList<>();
        this.participants.add(owner);
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public ArrayList<String> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<String> participants) {
        this.participants = participants;
    }

    public ArrayList<ListItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<ListItem> items) {
        this.items = items;
    }

    public void addItem(ListItem item) {
        this.items.add(item);
    }

    private String listName;
    private ArrayList<String> participants;
    private ArrayList<ListItem> items;
}
