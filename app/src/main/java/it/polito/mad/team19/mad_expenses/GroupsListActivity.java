package it.polito.mad.team19.mad_expenses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Adapters.GroupsAdapter;
import it.polito.mad.team19.mad_expenses.Classes.Group;

import static com.github.mikephil.charting.charts.Chart.LOG_TAG;

public class GroupsListActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "FirebaseLogged";
    private static final int REQUEST_INVITE = 6666;
    private static final int LOGIN_CHECK = 1;
    private static final int REQUEST_GROUP_CREATION = 2;
    private static final int GROUP_ACTIVITY = 999;
    private static final int ACCOUNT = 9;
    private static FirebaseDatabase myFirebaseDatabase;
    ListView groupListView;
    ArrayList<Group> groups = new ArrayList<>();
    protected FirebaseAuth.AuthStateListener mAuthStateListener;
    protected FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    String uid;
    String uName;
    ProgressBar progressBar;
    TextView debug_tv;
    RelativeLayout debug_ll;
    boolean firstTimeCheck = true;
    GoogleApiClient mGoogleApiClient;
    GroupsAdapter ga;
    Snackbar sb;
    IntentFilter filter;
    private BroadcastReceiver connectionReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(myFirebaseDatabase == null) {
            myFirebaseDatabase = FirebaseDatabase.getInstance();
                   myFirebaseDatabase.setPersistenceEnabled(true);
        }
        FirebaseApp.initializeApp(getApplicationContext());
        setContentView(R.layout.activity_groups_list);
        addAllViewListener();
    }

    private void addAllViewListener() {
        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        Log.e("Receiver","register start");
        registerConnectionReceiver(filter);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(AppInvite.API)
                .enableAutoManage(this, this)
                .build();


        userLogVerification();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));


        debug_tv = (TextView) findViewById(R.id.debug_tv);
        debug_ll = (RelativeLayout) findViewById(R.id.debug_ll);
        progressBar = (ProgressBar) findViewById(R.id.loading_pbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.groups_list_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(connectionReceiver!=null) {
                    closeSnack();
                    unregisterReceiver(connectionReceiver);
                    connectionReceiver = null;
                    Log.e("Receiver","unregister fab");
                }

                Intent i = new Intent(GroupsListActivity.this, CreateGroupActivity.class);
                startActivityForResult(i, REQUEST_GROUP_CREATION);
            }
        });


        groupListView = (ListView) findViewById(R.id.groups_lv);
        ga = new GroupsAdapter(GroupsListActivity.this, groups);
        groupListView.setAdapter(ga);

        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(connectionReceiver!=null) {
                    closeSnack();
                    unregisterReceiver(connectionReceiver);
                    connectionReceiver = null;
                    Log.e("Receiver","unregister group");
                }


                Intent intent = new Intent(GroupsListActivity.this, GroupActivity.class);
                intent.putExtra("groupName", ((Group) parent.getItemAtPosition(position)).getName());
                intent.putExtra("groupId", ((Group) parent.getItemAtPosition(position)).getGroupId());
                intent.putExtra("groupImage", ((Group) parent.getItemAtPosition(position)).getImage());
                intent.putExtra("groupMyBalance", ((Group) parent.getItemAtPosition(position)).getBalance().toString());
                startActivityForResult(intent,GROUP_ACTIVITY);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //updateList(uid);
        if(connectionReceiver==null) {
            registerConnectionReceiver(filter);
            Log.e("Receiver", "register on resum");
        }


        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //updateList(uid);


        if(connectionReceiver!=null) {
            closeSnack();
            unregisterReceiver(connectionReceiver);
            connectionReceiver = null;
            Log.e("Receiver","unregister on pause");
        }

        if(mAuth!=null)
            mAuth.removeAuthStateListener(mAuthStateListener);
    }



    private void userLogVerification() {
        mAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && !user.isAnonymous()) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_inGroup:" + user.getUid());
                    uid = user.getUid();
                    uName = user.getDisplayName();
                    if (uName == null)
                        uName = "User";
                    else if (uName.trim().isEmpty())
                        uName = "User";

                    if (firstTimeCheck) {
                        checkInvitations();
                        firstTimeCheck = false;
                    }
                } else {
                    // User is signed out

                    if(connectionReceiver!=null) {
                        closeSnack();
                        unregisterReceiver(connectionReceiver);
                        connectionReceiver = null;
                        Log.e("Receiver","unregister muauth");
                    }


                    groups.clear();
                    ga.notifyDataSetChanged();
                    Log.d(TAG, "onAuthStateChanged:signed_outGroup");
                    firstTimeCheck = true;

                    if(connectionReceiver!=null) {
                        closeSnack();
                        unregisterReceiver(connectionReceiver);
                        connectionReceiver = null;
                        Log.e("Receiver","unregister account");
                    }

                    Intent intent = new Intent(GroupsListActivity.this, GoogleSignInActivity.class);
                    progressBar.setVisibility(View.VISIBLE);
                    debug_tv.setVisibility(View.GONE);
                    debug_ll.setVisibility(View.GONE);
                    groupListView.setVisibility(View.INVISIBLE);
                    startActivityForResult(intent, LOGIN_CHECK);
                }
            }
        };
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_grouplist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.account:

                if(connectionReceiver!=null) {
                    closeSnack();
                    unregisterReceiver(connectionReceiver);
                    connectionReceiver = null;
                    Log.e("Receiver","unregister account");
                }


                Intent intent = new Intent(GroupsListActivity.this, AccountActivity.class);
                startActivityForResult(intent,ACCOUNT);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GROUP_CREATION: {

                if(connectionReceiver==null) {
                    registerConnectionReceiver(filter);
                    Log.e("Receiver","register group create");
                }
                if (resultCode == 1)
                    progressBar.setVisibility(View.VISIBLE);
                debug_tv.setVisibility(View.GONE);
                debug_ll.setVisibility(View.GONE);
                groupListView.setVisibility(View.INVISIBLE);
                updateList(uid);
                break;
            }

            case ACCOUNT:
            {
                if(connectionReceiver==null) {
                    registerConnectionReceiver(filter);
                    Log.e("Receiver","register account");
                }
                break;
            }
            case LOGIN_CHECK: {

                if(connectionReceiver==null) {
                    registerConnectionReceiver(filter);
                    Log.e("Receiver","register");
                }

                if (resultCode == 0)
                    finish();
                if (resultCode == 1 && firstTimeCheck) {
                    progressBar.setVisibility(View.VISIBLE);
                    debug_tv.setVisibility(View.GONE);
                    debug_ll.setVisibility(View.GONE);
                    groupListView.setVisibility(View.INVISIBLE);

                    checkInvitations();
                    firstTimeCheck = false;
                }
                break;
            }
            case GROUP_ACTIVITY:
                {

                    if(connectionReceiver==null) {
                        registerConnectionReceiver(filter);
                        Log.e("Receiver","register group acitivty");
                    }

                    //TODO Jured: controllare se update list va bene qui
                    updateList(uid);
                if(resultCode==99) {
                    //updateList(uid);
                }
                break;
            }
            default:
        }
    }

    void checkInvitations() {
        boolean autoLaunchDeepLink = true;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(new ResultCallback<AppInviteInvitationResult>() {
                    @Override
                    public void onResult(AppInviteInvitationResult result) {
                        Log.d(TAG, "getInvitation:onResult:" + result.getStatus());
                        if (result.getStatus().isSuccess()) {
                            // Extract information from the intent
                            Intent intent = result.getInvitationIntent();
                            String deepLink = AppInviteReferral.getDeepLink(intent);
                            String invitationId = AppInviteReferral.getInvitationId(intent);

                            Log.d("Invitations", "" + deepLink);
                            String deepLinkName;
                            String groupIdName;
                            String results[] = deepLink.split("/");
                            deepLinkName = results[0];

                            if (deepLinkName.equals("addPersonToGroup")) {
                                groupIdName = results[1];
                                Log.d("Invitations", "add person to group with id: " + groupIdName);
                                addGroupToUser(uid, groupIdName);
                            }

                        } else
                            updateList(uid);
                    }
                });
    }

    private void addGroupToUser(final String uid, final String groupIdName) {

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef2 = database.getReference("gruppi").child(groupIdName);
        myRef2.keepSynced(true);

        myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChildren()) {
                    Log.d("Group", snapshot.getValue().toString());

                    DatabaseReference addGroupRef;
                    addGroupRef = FirebaseDatabase.getInstance().getReference();
                    addGroupRef.child("gruppi").child(groupIdName).child("membri").child(uid).child("tipo").setValue(0);
                    addGroupRef.child("gruppi").child(groupIdName).child("membri").child(uid).child("nome").setValue(uName);
                    if(mAuth.getCurrentUser().getPhotoUrl()!=null)
                        addGroupRef.child("gruppi").child(groupIdName).child("membri").child(uid).child("immagine").setValue(mAuth.getCurrentUser().getPhotoUrl().toString());


                    addGroupRef.child("utenti").child(uid).child("gruppi").child(groupIdName).child("bilancio").setValue(0);
                    try {
                        addGroupRef.child("utenti").child(uid).child("gruppi").child(groupIdName).child("immagine").setValue(snapshot.child("immagine").getValue().toString());
                    } catch (NullPointerException e) {
                        Log.d("Group", "noimage");
                    }
                    addGroupRef.child("utenti").child(uid).child("gruppi").child(groupIdName).child("nome").setValue(snapshot.child("nome").getValue().toString());
                    addGroupRef.child("utenti").child(uid).child("gruppi").child(groupIdName).child("notifiche").setValue(0);
                    updateList(uid);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("GroupListActivity", "Group reading cancelled");
            }
        });
    }

    void updateList(String uid)
    {
        Log.e(LOG_TAG, "updateList");
        progressBar.setVisibility(View.VISIBLE);
        mAuth = FirebaseAuth.getInstance();
        groups.clear();
        ga.notifyDataSetChanged();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("utenti").child(uid).child("gruppi");
        mDatabase.keepSynced(true);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Log.d("ListenerForSingle", "messaggio che vuoi");

                if (snapshot.hasChildren()) {
                    progressBar.setVisibility(View.GONE);
                    debug_tv.setVisibility(View.GONE);
                    debug_ll.setVisibility(View.GONE);
                    groupListView.setVisibility(View.VISIBLE);
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Log.d("Invite", child.toString());

                        if (child.hasChild("immagine"))
                            groups.add(new Group(child.child("nome").getValue().toString(), Float.parseFloat(child.child("bilancio").getValue().toString()), Integer.parseInt(child.child("notifiche").getValue().toString()), child.child("immagine").getValue().toString(), child.getKey()));
                        else
                            groups.add(new Group(child.child("nome").getValue().toString(), Float.parseFloat(child.child("bilancio").getValue().toString()), Integer.parseInt(child.child("notifiche").getValue().toString()), null, child.getKey()));
                    }
                    ga.notifyDataSetChanged();

                } else {
                    progressBar.setVisibility(View.GONE);
                    groupListView.setVisibility(View.GONE);
                    debug_ll.setVisibility(View.VISIBLE);
                    debug_tv.setVisibility(View.VISIBLE);
                    debug_tv.setText(R.string.youre_not_part_of_any_group);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("GroupListActivity", "Unable to read user's groups");
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("GroupsListActivity", "Error in the connection:\n" + connectionResult.getErrorMessage());
    }

    void registerConnectionReceiver(IntentFilter filter)
    {
        connectionReceiver =
        new BroadcastReceiver()
        {
            boolean before = true;
            @Override
            public void onReceive(Context context, Intent intent) {
                isNetworkAvailable(context);
            }

            public boolean isNetworkAvailable(Context context) {
                ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                if (connectivity != null) {
                    NetworkInfo[] info = connectivity.getAllNetworkInfo();
                    if (info != null) {
                        for (int i = 0; i < info.length; i++) {
                            if (info[i].getState() == NetworkInfo.State.CONNECTED)
                            {
                                if(before==false) {
                                   updateList(uid);
                                }

                                if (sb!=null)
                                    sb.dismiss();
                                before = true;
                                Log.e(LOG_TAG, "Now you are connected to Internet!");
                                return true;
                            }
                        }
                    }
                }
                if (sb==null)
                    showSnackBar(context);
                else
                    sb.show();

                before=false;
                Log.e(LOG_TAG, "Now you are discconnected to Internet!");
                return false;
            }


            public void showSnackBar(final Context mContext)
            {

                    Snackbar.make(findViewById(android.R.id.content), mContext.getString(R.string.noConnectionSnack), Snackbar.LENGTH_INDEFINITE)
                            .setAction(mContext.getString(R.string.openSettings), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                    mContext.startActivity(intent);
                                }
                            })
                            .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                @Override
                                public void onShown(Snackbar transientBottomBar) {
                                    super.onShown(transientBottomBar);
                                    sb = transientBottomBar;
                                }
                            })
                            .show();


            }

        };
        registerReceiver(connectionReceiver,filter);
    }

    public void closeSnack()
    {
        if(sb!=null)
            sb.dismiss();
    }
}
