package com.example.craig.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.craig.myapplication.common.User;
import com.example.craig.myapplication.util.DownloadImageTask;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final Context ctx = this;
    private AllListsViewerFragment mainListViewer = new AllListsViewerFragment();
    private InvitesFragment invitesViewer = new InvitesFragment();
    public static String userID;
    public static String userName;
    public static String userEmail;
    public static String userPhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, mainListViewer).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = currentUser.getEmail();
        userName = currentUser.getDisplayName();
        userID = User.createUid(userEmail);
        userPhotoUrl = currentUser.getPhotoUrl().toString();

        if (currentUser != null) {
            View navHeader = navigationView.getHeaderView(0);
            TextView name = navHeader.findViewById(R.id.user_name);
            TextView email = navHeader.findViewById(R.id.user_email);
            ImageView img = navHeader.findViewById(R.id.user_image);

            try
            {
                name.setText(currentUser.getDisplayName());
                email.setText(currentUser.getEmail());
                new DownloadImageTask(img).execute(currentUser.getPhotoUrl().toString());
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, mainListViewer).commit();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_sign_out) {
            signOut();
        } else if (id == R.id.nav_lists) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, mainListViewer).commit();
        } else if (id == R.id.nav_invites) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, invitesViewer).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(myIntent);
            }
        });
    }
}
