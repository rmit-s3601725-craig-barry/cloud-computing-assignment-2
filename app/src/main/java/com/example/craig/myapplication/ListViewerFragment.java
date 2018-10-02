package com.example.craig.myapplication;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.craig.myapplication.common.CollabList;
import com.example.craig.myapplication.common.ListItem;
import com.example.craig.myapplication.common.User;
import com.example.craig.myapplication.util.FB;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListViewerFragment
    extends TitledFragment
{
    private String listName;
    private String listID;
    private View view;
    private ArrayList<ListItem> listItems = new ArrayList<>();
    private ValueEventListener listUpdateListener = null;

    public ListViewerFragment setListName(String listName)
    {
        this.listName = listName;
        return this;
    }

    public ListViewerFragment setListID(String listID)
    {
        this.listID = listID;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setFragmentTitle(listName);
        view = inflater.inflate(R.layout.fragment_list_viewer, container, false);

        listItems = new ArrayList<>();

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab2);
        fab.setOnClickListener(new View.OnClickListener() {

            private String m_Text = "";

            @Override
            public void onClick(final View view) {


                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Send List Participant Invite");
                // I'm using fragment here so I'm using getView() to provide ViewGroup
                // but you can provide here any other instance of ViewGroup from your Fragment / Activity
                View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.popup_txt_dialog, null);
                // Set up the input
                final EditText input = viewInflated.findViewById(R.id.editTextDialogUserInput);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                builder.setView(viewInflated);

                builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        inviteParticipant(m_Text);
                        Snackbar.make(view, "Sent invite to user: " + m_Text, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        FloatingActionButton addListItemBtn = (FloatingActionButton) view.findViewById(R.id.fabAddListItem);

        addListItemBtn.setOnClickListener(new View.OnClickListener() {

            private String m_Text = "";

            @Override
            public void onClick(final View view) {


                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Add List Item");
                View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.popup_txt_dialog, null);
                final EditText input = viewInflated.findViewById(R.id.editTextDialogUserInput);
                builder.setView(viewInflated);
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        addListItem(m_Text, false);
                        Snackbar.make(view, "Added item to list: " + m_Text, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        initListItems();

        return view;
    }

    private void initListItems()
    {
        LinearLayout root = view.findViewById(R.id.list_layout);

        listUpdateListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                try {
                    CollabList list = data.getValue(CollabList.class);
                    root.removeAllViews();
                    for(int i = 0; i < list.getItems().size(); i++)
                    {
                        ListItem listItem = list.getItems().get(i);
                        addListItemUI(listItem.getItem(), listItem.isChecked(), i);
                    }
                }
                catch(Exception e)
                {
                    throw e;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("lists/" + listID).addValueEventListener(listUpdateListener);
    }

    public void addListItemUI(String item, boolean checked, int index)
    {
        LinearLayout root = view.findViewById(R.id.list_layout);

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.list_item_icon, null);
        final TextView txt =  viewInflated.findViewById(R.id.list_name);
        final CheckBox checkBox = viewInflated.findViewById(R.id.checkbox);
        final CardView cardView = viewInflated.findViewById(R.id.list_item_card_view);
        txt.setText(item);
        checkBox.setChecked(checked);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FB.checkListItem(listID, index, isChecked);
            }
        });

        root.addView(viewInflated);
    }

    public void addListItem(String item, boolean checked)
    {
        FB.addListItem(listID, item);
    }

    private void inviteParticipant(String email) {
        String userID = User.createUid(email);
        FB.sendInvite(listID, listName, userID, MainActivity.userName, MainActivity.userPhotoUrl);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("lists/" + listID).removeEventListener(listUpdateListener);
    }
}
