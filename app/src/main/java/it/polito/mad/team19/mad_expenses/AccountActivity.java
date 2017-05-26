package it.polito.mad.team19.mad_expenses;

import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import it.polito.mad.team19.mad_expenses.Classes.NetworkChangeReceiver;

public class AccountActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {


    private static final String VALID_EMAIL_REGEX = "^(.+)@(.+)$";
    private static final String TAG = "FirebaseSignIn";
    FirebaseAuth.AuthStateListener mAuthListener;
    Button signOut;
    Button pswd_reset;
    ImageView edit_name;
    TextView email;
    TextView displayedName;
    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    NetworkChangeReceiver netChange;
    IntentFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        setTitle(getString(R.string.account));

        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netChange = new NetworkChangeReceiver();
        netChange.setViewForSnackbar(findViewById(android.R.id.content));
        netChange.setDialogShowTrue(false);
        registerReceiver(netChange, filter);

        signOut = (Button) findViewById(R.id.btn_signout);
        pswd_reset = (Button) findViewById(R.id.reset_passwd);
        edit_name = (ImageView) findViewById(R.id.edit_user_name);

        //TODO: permettere di modificare anche l'immagine

        if (mAuth.getCurrentUser().getProviders().contains("firebase")) {
            pswd_reset.setVisibility(View.VISIBLE);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        final GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mGoogleApiClient.isConnected())
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(@NonNull Status status) {
                                }
                            });

                mAuth.signOut();
                finish();
            }
        });


        edit_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Name", "edit");
                showAddContactDialog(getString(R.string.changename), displayedName.getText().toString());
            }
        });


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    displayedName = (TextView) findViewById(R.id.displayed_name_tv);
                    email = (TextView) findViewById(R.id.email_tv);
                    final ImageView userImg = (ImageView) findViewById(R.id.user_img);

                    if (user.getDisplayName() != null)
                        displayedName.setText(user.getDisplayName());

                    email.setText(user.getEmail());

                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);


                    Glide.with(getApplicationContext()
                    ).load(user.getPhotoUrl()).asBitmap().centerCrop().error(R.mipmap.ic_group).into(new BitmapImageViewTarget(userImg) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getResources(), resource);

                            circularBitmapDrawable.setCircular(true);
                            userImg.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                }
                // ...
            }
        };
        // ...
    }


    public void showAddContactDialog(String title, String old_string) {
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialogboxlayout_edit_account, null);
        final EditText new_string;

        new_string = (EditText) dialogView.findViewById(R.id.new_string);

        new_string.setText(old_string);

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle(title)
                .setPositiveButton(getString(R.string.edit), null)
                .setNegativeButton(getString(R.string.cancel), null)
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button buttonPositive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                buttonPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //if(!new_string.getText().toString().trim().matches(VALID_EMAIL_REGEX)) {
                        //    Snackbar.make(findViewById(android.R.id.content), getString(R.string.invalid_email), Snackbar.LENGTH_INDEFINITE).show();
                        //    dialog.cancel();
                        //}
                        if (new_string.getText().toString().trim().isEmpty()) {
                            new_string.setError(getString(R.string.mandatory_field));
                        }
                        else {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(new_string.getText().toString())
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("Name", "Name updated.");
                                                displayedName.setText(new_string.getText().toString());
                                                //TODO: modificare anche il nome nei vari campi members dei gruppi associati all'utente
                                                dialog.cancel();
                                            }
                                        }
                                    });
                        }
                    }
                });

                Button buttonNegative = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                buttonNegative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });

            }
        });
        alertDialog.show();

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (netChange == null) {
            netChange = new NetworkChangeReceiver();
            netChange.setViewForSnackbar(findViewById(android.R.id.content));
            netChange.setDialogShowTrue(false);
            registerReceiver(netChange, filter);
            Log.d("Receiver", "register on resum");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (netChange != null) {
            netChange.closeSnack();
            unregisterReceiver(netChange);
            netChange = null;
            Log.d("Receiver", "unregister on pause");
        }

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("AccountActivity", "Could not connect");
    }
}
