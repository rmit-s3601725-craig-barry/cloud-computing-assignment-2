package com.example.craig.myapplication.common;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CollabList
        extends FirebaseItem {

    public CollabList() {
        this.participants = new ArrayList<>();
        this.items = new HashMap<>();
    }

    public CollabList(String listName, String owner) {
        this.listName = listName;
        this.participants = new ArrayList<>();
        this.items = new HashMap<>();
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

    public Map<String, ListItem> getItems() {
        return items;
    }

    public void setItems(Map<String, ListItem> items) {
        this.items = items;
    }

    public void addItem(Pair<String, ListItem> item) {
        this.items.put(item.first, item.second);
    }

    public void removeItem(String listItemId)
    {
        items.remove(listItemId);
    }

    private String listName;
    private ArrayList<String> participants;
    private Map<String, ListItem> items;
}
