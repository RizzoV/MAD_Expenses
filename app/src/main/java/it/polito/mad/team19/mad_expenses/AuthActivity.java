package it.polito.mad.team19.mad_expenses;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Bolz on 07/04/2017.
 */

public class AuthActivity extends AppCompatActivity
{

    // declare auth
    private FirebaseAuth mAuth;

    // declare auth listener
    private FirebaseAuth.AuthStateListener mAuthListener;

    protected static final String TAG = "firebaseAuth";

    private String mCustomToken;
    //private TokenBroadcastReceiver mTokenReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
/*
        // Button click listeners (only if we have an Activity in which click sign in)
        findViewById(R.id.button_sign_in).setOnClickListener(this);

        // Create token receiver (for demo purposes only)
        mTokenReceiver = new TokenBroadcastReceiver()
        {
            @Override
            public void onNewToken(String token)
            {
                Log.d(TAG, "onNewToken:" + token);
                setCustomToken(token);
            }
        };

        // initialize auth
        mAuth = FirebaseAuth.getInstance();

        // start auth state listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // exclude
                updateUI(user);

            }
        };
        // end auth state listener
*/

    }
}
