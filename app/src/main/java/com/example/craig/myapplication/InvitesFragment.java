package com.example.craig.myapplication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.craig.myapplication.common.Invite;
import com.example.craig.myapplication.util.DownloadImageTask;
import com.example.craig.myapplication.util.FB;
import com.example.craig.myapplication.util.SetDifference;
import com.example.craig.myapplication.util.Transitions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvitesFragment
    extends TitledFragment
{
    private View view;
    private ValueEventListener invitesUpdateListener = null;
    private List<Invite> inviteList;
    private Map<String, View> inviteViews;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setFragmentTitle("Invites");
        view = inflater.inflate(R.layout.fragment_invites_viewer, container, false);
        inviteList = new ArrayList<>();
        inviteViews = new HashMap<>();
        initInvites();

        return view;
    }

    public void initInvites()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        invitesUpdateListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                List<Invite> nInviteList = new ArrayList<>();
                for(DataSnapshot child : data.getChildren())
                {
                    Invite inv = child.getValue(Invite.class);
                    nInviteList.add(inv);
                }

                List<Invite> addedInvites = SetDifference.getAdditions(inviteList, nInviteList);
                List<Invite> removedInvites = SetDifference.getRemovals(inviteList, nInviteList);

                inviteList = nInviteList;

                for(Invite inv : removedInvites)
                    removeInviteUI(inv);
                for(Invite inv : addedInvites)
                    addInviteUI(inv);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };

        database.getReference("invites/" + MainActivity.userID).addValueEventListener(invitesUpdateListener);
    }

    public void addInviteUI(Invite invite)
    {
        LinearLayout root = view.findViewById(R.id.list_layout);
        View inviteCard = LayoutInflater.from(getContext()).inflate(R.layout.invite_card, null);
        final ImageView img = inviteCard.findViewById(R.id.user_image);
        final TextView listNameField = inviteCard.findViewById(R.id.list_name);
        final TextView invitedByField = inviteCard.findViewById(R.id.invited_by);
        final FloatingActionButton btnAccept = inviteCard.findViewById(R.id.btn_accept);
        final FloatingActionButton btnDecline = inviteCard.findViewById(R.id.btn_decline);

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                root.removeView(inviteCard);
                FB.acceptInvite(invite);
                Snackbar.make(view, "Invitation Accepted", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                root.removeView(inviteCard);
                FB.clearInvite(invite);
                Snackbar.make(view, "Invitation Declined", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        //Asynchronously load image
        new DownloadImageTask(img).execute(invite.getUserPhotoUrl());
        listNameField.setText("List: " + invite.getListName());
        invitedByField.setText(invite.getUserName() + " has invited you to collaborate");
        inviteViews.put(invite.getUid(), inviteCard);

        Transitions.addElement(root, inviteCard);
    }

    private void removeInviteUI(Invite invite)
    {
        LinearLayout root = view.findViewById(R.id.list_layout);
        if(inviteViews.get(invite.getUid()) != null)
        {
            root.removeView(inviteViews.get(invite.getUid()));
            inviteViews.remove(invite.getUid());
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("invites/" + MainActivity.userID).removeEventListener(invitesUpdateListener);
    }
}
