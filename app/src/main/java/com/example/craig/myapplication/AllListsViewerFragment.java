package com.example.craig.myapplication;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.craig.myapplication.common.CollabList;
import com.example.craig.myapplication.common.User;
import com.example.craig.myapplication.util.FB;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AllListsViewerFragment
    extends TitledFragment
{
    private View view;
    private ArrayList<CollabList> lists = new ArrayList<>();
    private ValueEventListener userUpdateListener = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setFragmentTitle("Lists");
        view = inflater.inflate(R.layout.fragment_all_list_viewer, container, false);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            private String m_Text = "";

            @Override
            public void onClick(final View view) {


                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Create new list");
                // I'm using fragment here so I'm using getView() to provide ViewGroup
                // but you can provide here any other instance of ViewGroup from your Fragment / Activity
                View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.popup_txt_dialog, null);
                // Set up the input
                final EditText input = viewInflated.findViewById(R.id.editTextDialogUserInput);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                builder.setView(viewInflated);

                builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        addList(m_Text);
                        Snackbar.make(view, "Added new list: " + m_Text, Snackbar.LENGTH_LONG)
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

        initLists();

        return view;
    }

    public void initLists()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        userUpdateListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                User usr = data.getValue(User.class);
                for(String listId: usr.getListIds())
                {
                    LinearLayout root = view.findViewById(R.id.list_layout);
                    root.removeAllViews();
                    database.getReference("lists/" + listId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot data) {
                            try
                            {
                                CollabList list = data.getValue(CollabList.class);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        addListItem(list.getUid(), list.getListName());
                                    }
                                });
                            }
                            catch(Exception e) {
                                throw e;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };

        database.getReference("users/" + MainActivity.userID).addValueEventListener(userUpdateListener);
    }

    public void addListItem(final String listUid, final String listName)
    {
        LinearLayout root = view.findViewById(R.id.list_layout);

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.list_icon, null);
        final TextView txt =  viewInflated.findViewById(R.id.list_name);
        txt.setText(listName);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_main,
                        new ListViewerFragment().setListName(listName).setListID(listUid)).commit();
            }
        };

        viewInflated.setOnClickListener(clickListener);
        txt.setOnClickListener(clickListener);
        root.addView(viewInflated);
    }

    public void addList(final String listName)
    {
        CollabList newList = FB.createNewList(listName, MainActivity.userID);
        lists.add(newList);
//        addListItem(newList.getUid(), newList.getListName());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users/" + MainActivity.userID).removeEventListener(userUpdateListener);
    }



}
