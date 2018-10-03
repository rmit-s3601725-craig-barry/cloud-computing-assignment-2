package com.example.craig.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.example.craig.myapplication.global.Global;
import com.example.craig.myapplication.global.GlobalFactory;
import com.example.craig.myapplication.util.FB;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity
        extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener{

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "PlayActivity";

    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
        if(getActionBar() != null)
            getActionBar().hide();
        setContentView(R.layout.sign_in_layout);

        final Button signInButton = findViewById(R.id.googleSignInBtn);

        Global gbl = GlobalFactory.make();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog = new ProgressDialog(LoginActivity.this);
                loadingDialog.setMessage("Signing in ...");
                loadingDialog.setCancelable(false);
                loadingDialog.setInverseBackgroundForced(false);
                loadingDialog.show();

                gbl.signInGoogle(LoginActivity.this);
        }});
    }

    @Override
    public void onBackPressed()
    {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        connectionFailed();
    }

    private void connectionFailed()
    {
        loadingDialog.hide();
        Snackbar.make(findViewById(R.id.login_view), "Sign In Failed", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            // If Google ID authentication is successful, obtain a token for Firebase authentication.
            if (result.isSuccess() && result.getSignInAccount() != null) {

                AuthCredential credential = GoogleAuthProvider.getCredential(
                        result.getSignInAccount().getIdToken(), null);
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                                    if (currentUser != null) {
                                        FB.saveUser(currentUser);

                                        //This line forces the user to choose a new account when they log out
                                        //Otherwise the same account will be automatically logged in
                                        GlobalFactory.make().resetDefault();

                                        Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(myIntent);
                                        loadingDialog.hide();
                                    }
                                } else {
                                    connectionFailed();
                                    Log.w(TAG, "signInWithCredential:onComplete failed", task.getException());
                                }
                            }
                        });
            } else {
                connectionFailed();
            }
        } else {
            connectionFailed();
        }
    }
}
