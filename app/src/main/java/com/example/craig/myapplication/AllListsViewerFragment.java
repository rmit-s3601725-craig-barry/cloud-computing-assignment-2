package com.example.craig.myapplication;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.craig.myapplication.common.CollabList;
import com.example.craig.myapplication.common.User;
import com.example.craig.myapplication.util.ClickGestureDetector;
import com.example.craig.myapplication.util.DownloadImageTask;
import com.example.craig.myapplication.util.FB;
import com.example.craig.myapplication.util.SetDifference;
import com.example.craig.myapplication.util.Transitions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllListsViewerFragment
    extends TitledFragment
{
    private View view;
    private ArrayList<CollabList> lists = new ArrayList<>();
    private ValueEventListener userUpdateListener = null;
    private User curUser = null;
    private Map<String, View> listMap;
    private String selectedListId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setFragmentTitle("Lists");
        view = inflater.inflate(R.layout.fragment_all_list_viewer, container, false);
        listMap = new HashMap<>();

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
                        Snackbar.make(view, "Created new list: " + m_Text, Snackbar.LENGTH_LONG)
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

        curUser = null;
        initLists();

        return view;
    }

    public void initLists()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        userUpdateListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                List<String> addListIds = null;
                List<String> rmListIds = new ArrayList<>();
                User newUser = data.getValue(User.class);
                if(newUser == null || !data.exists())
                    return;
                else if(curUser == null) {
                    curUser = newUser;
                    addListIds = curUser.getListIds();
                }
                else
                {
                    addListIds = SetDifference.getAdditions(curUser.getListIds(), newUser.getListIds());
                    rmListIds = SetDifference.getRemovals(curUser.getListIds(), newUser.getListIds());
                }

                for(String listId: addListIds)
                {
                    LinearLayout root = view.findViewById(R.id.list_layout);
//                    root.removeAllViews();
                    database.getReference("lists/" + listId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot data) {
                            try
                            {
                                CollabList list = data.getValue(CollabList.class);
                                if(list != null)
                                {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            addListItem(list);
                                        }
                                    });
                                }
                            }
                            catch(Exception e) {
                                throw e;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }

                for(String listId: rmListIds)
                {
                    removeListItem(listId);
                }

                curUser = newUser;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };

        database.getReference("users/" + MainActivity.userID).addValueEventListener(userUpdateListener);
    }

    public void addListItem(final CollabList list)
    {
        LinearLayout root = view.findViewById(R.id.list_layout);
        final String listName = list.getListName();
        final String listUid = list.getUid();

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.list_icon, null);
        final TextView txt =  viewInflated.findViewById(R.id.list_name);
        final ImageButton btnSettings = viewInflated.findViewById(R.id.btn_settings);
        txt.setText(listName);

        registerForContextMenu(btnSettings);
        btnSettings.setOnClickListener((view ) -> {
            selectedListId = listUid;
            getActivity().openContextMenu(view);
        });
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_main,
                        new ListViewerFragment().setListName(listName).setListID(listUid)).commit();
            }
        };

        final LinearLayout userImgsLayout = viewInflated.findViewById(R.id.list_users);
        final HorizontalScrollView scrollView = viewInflated.findViewById(R.id.scroll_view);
        final ConstraintLayout layoutMgr = viewInflated.findViewById(R.id.layout_mgr);
        for(String userId : list.getParticipants())
        {
            View userPhotoView = LayoutInflater.from(getContext()).inflate(R.layout.circle_img, userImgsLayout, false);
            final ImageView img = userPhotoView.findViewById(R.id.user_image);
            userImgsLayout.addView(userPhotoView);
            userPhotoView.setOnClickListener(clickListener);

            FB.getUserPhotoUrl(userId, (userPhotoUrl) -> {
                getActivity().runOnUiThread( () -> {
                    new DownloadImageTask(img).execute(userPhotoUrl);
                });
            });
        }

        final GestureDetector detector = new GestureDetector(getContext(), new ClickGestureDetector((x) -> {
            clickListener.onClick(view);
        }));

        viewInflated.setOnClickListener(clickListener);
        txt.setOnClickListener(clickListener);
        userImgsLayout.setOnClickListener(clickListener);
        scrollView.setOnClickListener(clickListener);
        scrollView.setOnTouchListener((view, event) -> {
            detector.onTouchEvent(event);
            return false;
        });
        layoutMgr.setOnClickListener(clickListener);
        listMap.put(listUid, viewInflated);
        Transitions.addElement(root, viewInflated);
    }

    public void removeListItem(final String listUid) {
        LinearLayout root = view.findViewById(R.id.list_layout);
        if (listMap.get(listUid) != null) {
            root.removeView(listMap.get(listUid));
            listMap.remove(listUid);
        }
    }

    public void addList(final String listName)
    {
        CollabList newList = FB.createNewList(listName, MainActivity.userID);
        lists.add(newList);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu,v,menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.list_settings, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                FB.destroyList(selectedListId);
                removeListItem(selectedListId);
                return true;
            case R.id.menu_leave:
                FB.removeUserFromList(selectedListId, MainActivity.userID);
                removeListItem(selectedListId);
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users/" + MainActivity.userID).removeEventListener(userUpdateListener);
    }



}
