package com.example.craig.myapplication.common;

public abstract class FirebaseItem {

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof FirebaseItem)
        {
            return this.uid.equals(((FirebaseItem) o).uid);
        }

        return false;
    }

    private String uid;
}
