package com.example.craig.myapplication.common;

public class ListItem
    extends FirebaseItem
{
    public ListItem() {}

    public ListItem(String item) {
        this.item = item;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    private boolean checked = false;
    private String item;
}
