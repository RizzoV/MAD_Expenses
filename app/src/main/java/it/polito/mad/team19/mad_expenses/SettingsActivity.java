package it.polito.mad.team19.mad_expenses;

import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Locale;
import java.util.Set;

import it.polito.mad.team19.mad_expenses.Adapters.CurrenciesAdapter;
import it.polito.mad.team19.mad_expenses.Classes.NetworkChangeReceiver;
import it.polito.mad.team19.mad_expenses.NotActivities.CurrenciesListGetter;

public class SettingsActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {


    private static final String VALID_EMAIL_REGEX = "^(.+)@(.+)$";
    private static final String TAG = "FirebaseSignIn";
    FirebaseAuth.AuthStateListener mAuthListener;
    Button signOut;
    ImageView edit_name;
    TextView email;
    TextView displayedName;
    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    NetworkChangeReceiver netChange;
    IntentFilter filter;
    AlertDialog alertDialog;
    Spinner currenciesSpinner;
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView image;
    private Toolbar toolbar;

    private ArrayList<String> currenciesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(getString(R.string.settings));

        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netChange = new NetworkChangeReceiver();
        netChange.setViewForSnackbar(findViewById(android.R.id.content));
        netChange.setDialogShowTrue(false);
        registerReceiver(netChange, filter);

        signOut = (Button) findViewById(R.id.btn_signout);
        edit_name = (ImageView) findViewById(R.id.edit_user_name);
        currenciesSpinner = (Spinner) findViewById(R.id.currencies_spinner);

        image = (ImageView) findViewById(R.id.user_setting_toolbar_image_iv);
        toolbar = (Toolbar) findViewById(R.id.user_setting_tb);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.user_setting_ctb);

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        SharedPreferences currencyPreference = getSharedPreferences("currencySetting", MODE_PRIVATE);

        // Vale: listener spinner
        currenciesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor editor = currencyPreference.edit();
                editor.putString("currency", ((String)parent.getItemAtPosition(position)).split("\t ")[0]);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("Spinner Currencies", "Nothing selected, do nothing");
            }
        });

        // Vale: Populate the currencies spienner
        // Genera lista di valute
        Set<Currency> currencies = (new CurrenciesListGetter(this)).getAvailableCurrencies();
        for (Currency currency : currencies) {
            try {
                String listItem;
                if(!currency.getCurrencyCode().equals(currency.getDisplayName()))
                    listItem = currency.getCurrencyCode() + "\t " + currency.getDisplayName();
                else
                    listItem = currency.getCurrencyCode();
                currenciesList.add(listItem);
            } catch (Exception e) {
                Log.e("AddExpenseActivity", "Error in the currencies management: " + e.getMessage());
            }
        }

        Collections.sort(currenciesList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });


        CurrenciesAdapter currenciesAdapter = new CurrenciesAdapter(this, currenciesList);
        currenciesSpinner.setAdapter(currenciesAdapter);
        currenciesSpinner.setPrompt(getString(R.string.currencies_spinner_prompt));

        // Vale: default spinner
        if(currencyPreference.getString("currency", "").equals(""))
            currenciesSpinner.setSelection(currenciesAdapter.searchInCurrenciesCodes(Currency.getInstance(Locale.getDefault()).getCurrencyCode()));
        else
            currenciesSpinner.setSelection(currenciesAdapter.searchInCurrenciesCodes(currencyPreference.getString("currency", "")));


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
                    //final ImageView userImg = (ImageView) findViewById(R.id.user_img);

                    if (user.getDisplayName() != null)
                        displayedName.setText(user.getDisplayName());

                    email.setText(user.getEmail());

                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);


                    Glide.with(getApplicationContext()
                    ).load(user.getPhotoUrl()).asBitmap().centerCrop().error(R.mipmap.ic_group).into(new BitmapImageViewTarget(image) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getResources(), resource);

                            circularBitmapDrawable.setCircular(true);
                            image.setImageBitmap(resource);
                            //userImg.setImageDrawable(circularBitmapDrawable);
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

        alertDialog = new AlertDialog.Builder(this)
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

        if(alertDialog != null)
            if(alertDialog.isShowing())
                alertDialog.dismiss();

        if (netChange != null) {
            netChange.closeSnack();
            unregisterReceiver(netChange);
            netChange = null;
            Log.d("Receiver", "unregister on pause");
        }

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("SettingsActivity", "Could not connect");
    }
}
