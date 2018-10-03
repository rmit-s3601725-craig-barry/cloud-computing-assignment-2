package com.example.craig.myapplication.global;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.example.craig.myapplication.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;

public class Global
{
    public Global()
    {}

    public void signInGoogle(FragmentActivity c)
    {
        initGoogleApiClient(c);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        c.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void initGoogleApiClient(FragmentActivity c)
    {
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(c.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
        mGoogleApiClient = new GoogleApiClient.Builder(c)
                .enableAutoManage(c, (GoogleApiClient.OnConnectionFailedListener)c)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    public void resetDefault()
    {
        mGoogleApiClient.clearDefaultAccountAndReconnect();
    }

    private static GoogleApiClient mGoogleApiClient = null;

    private static final int RC_SIGN_IN = 9001;
}
