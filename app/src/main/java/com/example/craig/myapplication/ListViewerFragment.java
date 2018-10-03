package com.example.craig.myapplication;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.CardView;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.craig.myapplication.common.CollabList;
import com.example.craig.myapplication.common.ListItem;
import com.example.craig.myapplication.common.User;
import com.example.craig.myapplication.util.FB;
import com.example.craig.myapplication.util.LoadingDialog;
import com.example.craig.myapplication.util.SetDifference;
import com.example.craig.myapplication.util.Transitions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListViewerFragment
    extends TitledFragment
{
    private String listName;
    private String listID;
    private View view;
    private ArrayList<ListItem> listItems;
    private Map<String, View> listItemViews;
    private CollabList curList;
    private ValueEventListener listUpdateListener = null;
    private String selectedListItemId;

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

        LoadingDialog.showLoading(getContext(), "Loading...");

        listItems = new ArrayList<>();
        listItemViews = new HashMap<>();
        curList = null;

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab2);
        fab.setOnClickListener(new View.OnClickListener() {

            private String m_Text = "";

            @Override
            public void onClick(final View view) {


                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Invite to List");
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
                if(data != null && data.exists())
                {
                    CollabList list = data.getValue(CollabList.class);
                    if(curList != null)
                    {
                        Collection<ListItem> curListItems = curList.getItems().values();
                        Collection<ListItem> newListItems = list.getItems().values();

                        List<ListItem> addedListItems = SetDifference.getAdditions(curListItems, newListItems);
                        List<ListItem> removedListItems = SetDifference.getRemovals(curListItems, newListItems);


                        for(ListItem listItem : addedListItems)
                            addListItemUI(listItem.getItem(), listItem.isChecked(), listItem.getUid());
                        for(ListItem listItem : removedListItems)
                            removeListItem(listItem.getUid());
                        for(ListItem listItem : newListItems)
                        {
                            ListItem cListItem = curList.getItems().get(listItem.getUid());
                            if(cListItem != null && (listItem.isChecked() != cListItem.isChecked()))
                                setListItemChecked(listItem.getUid(), listItem.isChecked());
                        }

                    }
                    else
                    {
                        LoadingDialog.stopLoading();
                        for(ListItem listItem : list.getItems().values())
                            addListItemUI(listItem.getItem(), listItem.isChecked(), listItem.getUid());
                    }

                    curList = list;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("lists/" + listID).addValueEventListener(listUpdateListener);
    }

    public void addListItemUI(String item, boolean checked, String listItemId)
    {
        LinearLayout root = view.findViewById(R.id.list_layout);

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.list_item_icon, null);
        final TextView txt =  viewInflated.findViewById(R.id.list_name);
        final CheckBox checkBox = viewInflated.findViewById(R.id.checkbox);
        final ImageButton btnSettings = viewInflated.findViewById(R.id.btn_settings);
        txt.setText(item);
        checkBox.setChecked(checked);

        registerForContextMenu(btnSettings);

        btnSettings.setOnClickListener((view ) -> {
            selectedListItemId = listItemId;
            getActivity().openContextMenu(view);
        });
        txt.setOnClickListener((view) -> {
            checkBox.setChecked(!checkBox.isChecked());
        });
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FB.checkListItem(listID, listItemId, isChecked);
        });
        listItemViews.put(listItemId, viewInflated);
        Transitions.addElement(root, viewInflated);
    }

    public void addListItem(String item, boolean checked)
    {
        FB.addListItem(listID, item);
    }

    public void removeListItem(String listItemId)
    {
        LinearLayout root = view.findViewById(R.id.list_layout);
        if (listItemViews.get(listItemId) != null) {
            Transitions.removeElement(root, listItemViews.get(listItemId));
            listItemViews.remove(listItemId);
        }
    }

    public void setListItemChecked(String listItemId, boolean checked)
    {
        if (listItemViews.get(listItemId) != null) {
            View listItemView = listItemViews.get(listItemId);
            final CheckBox checkBox = listItemView.findViewById(R.id.checkbox);
            checkBox.setChecked(checked);
        }
    }

    private void inviteParticipant(String email) {
        String userID = User.createUid(email);
        FB.sendInvite(listID, listName, userID, MainActivity.userName, MainActivity.userPhotoUrl);
    }

    //Used to remove setOptionalIconsVisible linting bug, as shown here:
    //https://stackoverflow.com/questions/48607853/menubuilder-setoptionaliconsvisible-can-only-be-called-from-within-the-same-libr
    @SuppressLint("RestrictedApi")
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu,v,menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.list_item_settings, menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                FB.rmListItem(listID, selectedListItemId);
                removeListItem(selectedListItemId);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("lists/" + listID).removeEventListener(listUpdateListener);
    }

    private static final Object transObject = new Object();
}
