package com.example.craig.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

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
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener{

    public static GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "PlayActivity";

    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_layout);

        final Button signInButton = findViewById(R.id.googleSignInBtn);

        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog = new ProgressDialog(LoginActivity.this);
                loadingDialog.setMessage("Signing in ...");
                loadingDialog.setCancelable(false);
                loadingDialog.setInverseBackgroundForced(false);
                loadingDialog.show();

                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                // Start authenticating with Google ID first.
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

    }

    @Override
    public void onBackPressed()
    {}

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

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

    private void saveUserUid(FirebaseUser user)
    {

    }
}
