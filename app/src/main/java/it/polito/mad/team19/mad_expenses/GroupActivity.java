package it.polito.mad.team19.mad_expenses;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionItemTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

import it.polito.mad.team19.mad_expenses.Adapters.ExpensesRecyclerAdapter;
import it.polito.mad.team19.mad_expenses.Adapters.NotificationsAdapter;
import it.polito.mad.team19.mad_expenses.Adapters.ProposalsRecyclerAdapter;
import it.polito.mad.team19.mad_expenses.Classes.Expense;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseExpense;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseProposal;
import it.polito.mad.team19.mad_expenses.Classes.Me;
import it.polito.mad.team19.mad_expenses.Classes.NetworkChangeReceiver;
import it.polito.mad.team19.mad_expenses.Classes.Notifications;
import it.polito.mad.team19.mad_expenses.Classes.Proposal;

enum TabsList {
    EXPENSES,
    PROPOSALS
}

public class GroupActivity extends AppCompatActivity {

    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_NEW_EXPENSE = 2;
    private static final int REQUEST_NEW_PROPOSAL = 3;
    private static final int GROUP_INFO_REQUEST = 4;
    private static final int GROUP_QUITTED = 99;
    private static final int MODIFIED = 8;

    private PagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private DrawerLayout notificationsDrawer;
    private String groupName;
    private String groupId;
    private String groupImageUrl;
    private TabsList selectedTab;
    private ListView notificationsListView;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private final ArrayList<FirebaseGroupMember> groupMembersList = new ArrayList<FirebaseGroupMember>();
    private Drawable logo;
    private Toolbar toolbar;
    private ProgressDialog barProgressDialog;
    private ArrayList<Me> balancesArrayTakenFromFragment = new ArrayList<>();
    private int times = 0;

    NetworkChangeReceiver netChange;
    IntentFilter filter;

    protected static final String TAG = "firebaseAuth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netChange = new NetworkChangeReceiver();
        netChange.setViewForSnackbar(findViewById(android.R.id.content));
        netChange.setDialogShowTrue(false);
        registerReceiver(netChange, filter);

        // Get the intent which has started this activity
        Intent intent = getIntent();

        // Set the activity name retrieving it by the extras of the intent
        groupName = intent.getStringExtra("groupName");
        groupId = intent.getStringExtra("groupId");
        groupImageUrl = intent.getStringExtra("groupImage");

        // Initially the displayed tab will be the EXPENSES one
        selectedTab = TabsList.EXPENSES;

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        notificationsDrawer = (DrawerLayout) findViewById(R.id.notifications_drawer);
        notificationsListView = (ListView) findViewById(R.id.notification_lv);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView toolbarTitle = (TextView) findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(groupName);

        final ImageView logo = (ImageView) findViewById(R.id.toolbarLogo);

        // Manage group image

        Glide.with(this).load(groupImageUrl).asBitmap().centerCrop().error(R.mipmap.ic_group).into(new BitmapImageViewTarget(logo) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(getResources(), resource);

                circularBitmapDrawable.setCircular(true);
                logo.setImageDrawable(circularBitmapDrawable);
            }
        });


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupActivity.this, GroupInfoActivity.class);
                intent.putExtra("groupImage", groupImageUrl);
                intent.putExtra("groupName", groupName);
                intent.putExtra("groupId", groupId);
                startActivityForResult(intent, GROUP_INFO_REQUEST);
                Log.d("BolzDebug", "mannaiaBBolz");
            }
        });


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                switch (position) {
                    case 0:
                        selectedTab = TabsList.EXPENSES;
                        break;
                    case 1:
                        selectedTab = TabsList.PROPOSALS;
                        break;
                }

                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                if (!fab.isShown())
                    fab.show();
            }

            @Override
            public void onPageSelected(int position) {
                // Nothing (for now)
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Nothing (for now)
            }
        });


        // Set the click listener on the FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i;
                int requestCode;
                switch (selectedTab) {
                    case EXPENSES:
                        i = new Intent(GroupActivity.this, AddExpenseActivity.class);
                        requestCode = REQUEST_NEW_EXPENSE;
                        i.putExtra("groupId", groupId);
                        break;
                    case PROPOSALS:
                        i = new Intent(GroupActivity.this, AddProposalActivity.class);
                        requestCode = REQUEST_NEW_PROPOSAL;
                        i.putExtra("groupId", groupId);
                        break;
                    default:
                        i = new Intent(GroupActivity.this, AddExpenseActivity.class);
                        requestCode = REQUEST_NEW_EXPENSE;
                        break;
                }

                startActivityForResult(i, requestCode);
            }
        });

    }

    public int convertDipToPixels(float dips) {
        return (int) (dips * getApplicationContext().getResources().getDisplayMetrics().density + 0.5f);
    }


    private void firebaseAuth() {

        mAuth = FirebaseAuth.getInstance();
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
                // ...
            }
        };

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
                            Toast.makeText(GroupActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group, menu);

        // Retrieve the notification count textView from the menu
        MenuItem item = menu.findItem(R.id.notifications_icon);
        MenuItemCompat.setActionView(item, R.layout.notifications_ab_layout);
        RelativeLayout notifCount = (RelativeLayout) MenuItemCompat.getActionView(item);

        // Set up a listener which is able to get the number of notifications for the user in this group
        final TextView tv = (TextView) notifCount.findViewById(R.id.counter);
        final ImageView im = (ImageView) notifCount.findViewById(R.id.notifications_icon_action);

        final ArrayList<DataSnapshot> notificationsList = new ArrayList<DataSnapshot>();
        final NotificationsAdapter adapter = new NotificationsAdapter(GroupActivity.this, notificationsList, null);
        notificationsListView.setAdapter(adapter);


        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference().child("notifications").child(groupId);
        notificationRef.orderByKey().addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChildren()) {
                    int i = 0;
                    setNotificationNumber(tv);
                    notificationsList.clear();
                    adapter.notifyDataSetChanged();
                    //aggiorno lista notifiche
                    for (final DataSnapshot current : dataSnapshot.getChildren()) {

                        if (i == dataSnapshot.getChildrenCount() - 1) {
                            //imposto ultima notifica letta quando apro drawer
                            final DatabaseReference notificationRefUser = FirebaseDatabase.getInstance().getReference().child("utenti").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("gruppi").child(groupId).child("notifiche");
                            tv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.d("Clicked", "click");
                                    notificationsDrawer.openDrawer(Gravity.RIGHT);
                                    notificationRefUser.setValue(current.getKey());
                                    setNotificationNumber(tv);
                                }
                            });

                            im.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.d("Clicked", "click");
                                    notificationsDrawer.openDrawer(Gravity.RIGHT);
                                    notificationRefUser.setValue(current.getKey());
                                    setNotificationNumber(tv);
                                    //aggiungere listener per aggionare ultima notifica

                                }
                            });
                        }

                        notificationsList.add(current);
                        adapter.notifyDataSetChanged();

                        i++;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference groupStatusRef = FirebaseDatabase.getInstance().getReference()
                .child("gruppi").child(groupId).child("stato");

        groupStatusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null)
                    if(dataSnapshot.getValue().toString().compareTo("created") == 0)
                        showCaseHint();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return true;
    }

    private void showCaseHint () {
        final Toolbar  mToolbar = (Toolbar) findViewById(R.id.toolbar);

        ViewTarget navigationButtonViewTarget = new ViewTarget(mToolbar);

        final ShowcaseView showcaseView = new ShowcaseView.Builder(this)
                .setStyle(R.style.CustomShowcaseMaterial)
                .withMaterialShowcase()
                .setTarget(navigationButtonViewTarget)
                .build();

        showcaseView.setContentTitle(getString(R.string.new_group_add_members));
        showcaseView.setContentText(getString(R.string.add_new_members_hint));
        showcaseView.setButtonText(getString(R.string.got_it));
        showcaseView.hideButton();

        float density = getResources().getDisplayMetrics().density;
        int paddingDp = (int)(45 * density);

        showcaseView.setPadding(paddingDp,paddingDp,paddingDp,paddingDp);

        Target reOrderTarget = new Target() {
            @Override
            public Point getPoint() {
                return new ViewTarget(mToolbar.findViewById(R.id.person_add)).getPoint();
            }
        };

        showcaseView.setShowcase(reOrderTarget, true);

        showcaseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DebugShowcase", "onClick GotIt!");
                FirebaseDatabase.getInstance().getReference()
                        .child("gruppi").child(groupId).child("stato").setValue("seen");

                ActionMenuItemView addMemberMenuItem = (ActionMenuItemView) findViewById(R.id.person_add);
                addMemberMenuItem.performClick();


                showcaseView.hide();
            }
        });

        showcaseView.overrideButtonClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DebugShowcase", "onClick GotIt!");
                FirebaseDatabase.getInstance().getReference()
                        .child("gruppi").child(groupId).child("stato").setValue("seen");
                showcaseView.hide();
            }
        });
    }

    public void setNotificationNumber(final TextView tv) {
        DatabaseReference myNotRef = FirebaseDatabase.getInstance().getReference().child("utenti").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("gruppi").child(groupId).child("notifiche");
        myNotRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String mynot = dataSnapshot.getValue().toString();

                    //Prendo il numero di notifiche
                    final DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference().child("notifications").child(groupId);

                    ValueEventListener getGroupAndNotifcations = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int notNum = (int) dataSnapshot.getChildrenCount() - 1;

                            if (notNum > 0) {
                                tv.setText(String.valueOf(notNum));
                                tv.setVisibility(View.VISIBLE);
                            } else
                                tv.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("GroupActivity", "Could not get group and notificatons");
                        }
                    };

                    if (mynot != null && !mynot.equals("0")) {
                        notificationRef.orderByKey().startAt(mynot).addListenerForSingleValueEvent(getGroupAndNotifcations);
                    } else
                        notificationRef.addListenerForSingleValueEvent(getGroupAndNotifcations);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.notifications_icon:
                return true;

            case android.R.id.home: {
                finish();
                return true;
            }

            case R.id.person_add: {
                onInviteClicked();
                return true;
            }

            case R.id.personal_profile_icon: {
                Intent intent = new Intent(GroupActivity.this, MeActivity.class);
                intent.putExtra("groupId", groupId);
                /* VALE
                 * Crea un bundle contenente le informazioni su spese per non doverle riscaricare
                 * nella activity sui dettagli del gruppo
                 */
                Bundle b = new Bundle();
                b.putParcelableArrayList("balancesArray", balancesArrayTakenFromFragment);
                intent.putExtra("balancesBundle", b);
                startActivity(intent);
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQUEST_NEW_EXPENSE  && resultCode == RESULT_OK) || resultCode == MODIFIED) {
            // Gestione calcolo debiti e crediti dovuti alla nuova spesa

            Log.d("ExpenseIDActivity", data.getStringExtra("expenseId"));
            if (groupMembersList.size() > 0)
                groupMembersList.clear();

            ArrayList<FirebaseGroupMember> contributors = data.getParcelableArrayListExtra("contributors");
            ArrayList<FirebaseGroupMember> excluded = data.getParcelableArrayListExtra("excluded");




            //TODO: far avviare tutto in un thread
            Log.e("DEBUG", "IN");
            calculateBalances(data.getStringExtra("expenseId"), Float.parseFloat(data.getStringExtra("expenseTotal")), data.getStringExtra("expenseUId"),
                    data.getStringExtra("expenseUserName"), contributors, excluded);
        }

        if (requestCode == GROUP_INFO_REQUEST) {
            if (data != null) {
                if (data.hasExtra("newGroupName")) {
                    TextView toolbarTitle = (TextView) findViewById(R.id.toolbarTitle);
                    toolbarTitle.setText(data.getExtras().getString("newGroupName"));
                    groupName = data.getExtras().getString("newGroupName");
                }
            }
            if (resultCode == 99) {
                Log.d("DebugGroupQuitted", "GROUP_QUITTED result detected");
                setResult(99);
                finish();
            }
        }
    }

    private void calculateBalances (final String expenseId, final float expenseTotal, final String expenseUuid, final String expenseUserName,
                                   final ArrayList<FirebaseGroupMember> contributors, final ArrayList<FirebaseGroupMember> excluded) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("gruppi").child(groupId).child("membri");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.d("MembriSnap", dataSnapshot.getValue().toString());
                    if (child.child("immagine").exists())
                        groupMembersList.add(new FirebaseGroupMember(child.child("nome").getValue(String.class), child.child("immagine").getValue(String.class), child.getKey()));
                    else
                        groupMembersList.add(new FirebaseGroupMember(child.child("nome").getValue(String.class), null, child.getKey()));
                }
               // BalanceCalculator.calculate(groupId, expenseId, groupMembersList, expenseTotal, contributors, excluded);

                /////////////////////JURED PROVA//////////////////////////


                AsyncFirebaseBalanceLoader async = new AsyncFirebaseBalanceLoader(groupId, expenseId, groupMembersList, expenseTotal, contributors, excluded);
                async.execute();

                //////////////////////////////////////////////////////////


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("GroupActivity", "Unable to read group members");
            }
        });
    }

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

    private void onInviteClicked()
    {
        DatabaseReference getLastNotRef = FirebaseDatabase.getInstance().getReference().child("notifications").child(groupId);
        getLastNotRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String lastNotKey = "0";
                for(DataSnapshot not : dataSnapshot.getChildren())
                    lastNotKey = not.getKey();

                Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                        .setMessage(getString(R.string.invitation_message))
                        .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link) + "/" + groupId+"/"+lastNotKey))
                        .setCallToActionText(getString(R.string.invitation_cta))
                        .build();
                startActivityForResult(intent, REQUEST_INVITE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void passBalancesArray(Collection<Me> balancesArray) {
        balancesArrayTakenFromFragment.clear();
        for (Me b : balancesArray)
            balancesArrayTakenFromFragment.add(b);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return ExpensesListFragment.newInstance(position + 1);
                case 1:
                    //nuovo fragment delle proposte
                    return ProposalsListFragment.newInstance(position + 1);
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.tab_expenses);
                case 1:
                    return getResources().getString(R.string.tab_proposals);
            }
            return null;
        }
    }

}
