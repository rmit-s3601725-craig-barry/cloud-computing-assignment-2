package com.example.craig.myapplication.util;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.example.craig.myapplication.common.CollabList;
import com.example.craig.myapplication.common.Invite;
import com.example.craig.myapplication.common.ListItem;
import com.example.craig.myapplication.common.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FB
{
    private FB() {}

    public static void saveUser(FirebaseUser user)
    {
        final String userEmail = user.getEmail();
        final String userPhotoUri = user.getPhotoUrl().toString();
        final String userUid = User.createUid(userEmail);
        final String userRefPath = USERS + DELIM + userUid;

        changeUserData(userUid, (usr) -> {
                usr.setPhotoUrl(userPhotoUri);
                usr.setUid(userUid);
                FirebaseDatabase.getInstance().getReference(userRefPath).setValue(usr);
        });
    }

    public static CollabList createNewList(String listName, String userUid)
    {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference list_ref = database.getReference(LISTS);
        DatabaseReference ref = list_ref.push();
        CollabList newList = new CollabList(listName, userUid);
        final String listUid = ref.getKey();
        newList.setUid(listUid);
        ref.setValue(newList);

        final String userRefPath = USERS + DELIM + userUid;
        changeUserData(userUid, (usr) -> {
            usr.addList(listUid);
            usr.setUid(userUid);
        });

        return newList;
    }

    public static void sendInvite(String listId, String listName, String targetUserId, String srcUserName, String srcUserPhotoUrl)
    {
        Invite inv = new Invite(targetUserId, listId);
        inv.setListName(listName);
        inv.setUserName(srcUserName);
        inv.setUserPhotoUrl(srcUserPhotoUrl);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference invitesRef = database.getReference(INVITES + DELIM + targetUserId);
        DatabaseReference newInvRef = invitesRef.push();
        inv.setUid(newInvRef.getKey());
        newInvRef.setValue(inv);
    }

    public static void acceptInvite(Invite invite)
    {
        //Removes the invite from the database
        clearInvite(invite);

        changeUserData(invite.getUserId(), (usr) -> {
            usr.addList(invite.getListId());
        });
    }

    public static void clearInvite(Invite invite)
    {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference invsRef = database.getReference(INVITES + DELIM + invite.getUserId() + DELIM + invite.getUid());
        invsRef.setValue(null);

    }

    public static void addListItem(final String listId, final String item)
    {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        changeListData(listId, (list) -> {
            ListItem listItem = new ListItem(item);
            final DatabaseReference listRef = database.getReference(LISTS + DELIM + listId + DELIM + ITEMS);
            final DatabaseReference itemRef = listRef.push();
            listItem.setUid(itemRef.getKey());
            list.addItem(new Pair<>(listItem.getUid(), listItem));
        });
    }

    public static void rmListItem(final String listId, final String listItemId)
    {
        changeListData(listId, (list) -> {
            list.removeItem(listItemId);
        });
    }

    public static void checkListItem(final String listId, final String listItemId, final boolean checked)
    {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        String checkedPath = LISTS + DELIM + listId + DELIM + ITEMS + DELIM + listItemId + DELIM + CHECKED;
        final DatabaseReference checkedRef = database.getReference(checkedPath);
        checkedRef.setValue(checked);
    }

    private static void changeUserData(String userId, final Callback<User> l)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String userRefPath = USERS + DELIM + userId;
        final DatabaseReference userRef = database.getReference(userRefPath);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                User usr = new User();

                if(data.exists())
                {
                    usr = data.getValue(User.class);
                }

                l.callback(usr);
                FirebaseDatabase.getInstance().getReference(userRefPath).setValue(usr);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }


    private static void changeListData(String listId, final Callback<CollabList> l)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String listRefPath = LISTS + DELIM + listId;
        final DatabaseReference listRef = database.getReference(listRefPath);

        listRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                if(data.exists())
                {
                    CollabList list = data.getValue(CollabList.class);
                    l.callback(list);
                    FirebaseDatabase.getInstance().getReference(listRefPath).setValue(list);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

//    public static final void clearEventListeners()
//    {
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//
//        if(listChangedCallback != null)
//        {
//            final String listRefPath = LISTS + DELIM + listId;
//            final DatabaseReference listRef = database.getReference(listRefPath);
//            listRef.removeEventListener();
//        }
//    }
//
//    public static final void listenForEvents(
//            Callback<CollabList> listChanged,
//            Callback<User> userChangedCallback,
//            Callback<Invite> inviteChangedCallback)
//    {
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        final String listRefPath = LISTS + DELIM + listId;;
//
//
//
//    }

    private static Callback<CollabList> listChangedCallback = null;

    private static final String DELIM = "/";
    private static final String USERS = "users";
    private static final String LISTS = "lists";
    private static final String INVITES = "invites";
    private static final String ITEMS = "items";
    private static final String CHECKED = "checked";
}
