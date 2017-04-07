package it.polito.mad.team19.mad_expenses;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

import it.polito.mad.team19.mad_expenses.Adapters.ExpensesRecyclerAdapter;
import it.polito.mad.team19.mad_expenses.Adapters.ProposalsRecyclerAdapter;
import it.polito.mad.team19.mad_expenses.Classes.Expense;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseExpense;
import it.polito.mad.team19.mad_expenses.Classes.Proposal;

enum TabsList {
    EXPENSES,
    PROPOSALS
}

public class GroupActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private PagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    String name;
    TabsList selectedTab;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    protected static final String TAG = "firebaseAuth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        //Autenticazione anonima provvisoria
        //firebaseAuth();

        // Get the intent which has started this activity
        Intent intent = getIntent();

        // Set the activity name retrieving it by the extras of the intent
        name = intent.getStringExtra("group");
        setTitle(name);

        // Initially the displayed tab will be the EXPENSES one
        selectedTab = TabsList.EXPENSES;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.circle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                if(!fab.isShown())
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
                switch (selectedTab) {
                    case EXPENSES:
                        i = new Intent(GroupActivity.this, AddExpenseActivity.class);
                        break;
                    case PROPOSALS:
                        i = new Intent(GroupActivity.this, AddProposalActivity.class);
                        break;
                    default:
                        i = new Intent(GroupActivity.this, AddExpenseActivity.class);
                        break;
                }

                startActivity(i);
            }
        });
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

    /*
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    */


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group, menu);

        MenuItem item = menu.findItem(R.id.notifications_icon);

        MenuItemCompat.setActionView(item, R.layout.notifications_ab_layout);
        RelativeLayout notifCount = (RelativeLayout) MenuItemCompat.getActionView(item);

        int notCount = 10;

        TextView tv = (TextView) notifCount.findViewById(R.id.counter);
        ImageView im = (ImageView) notifCount.findViewById(R.id.notifications_icon_action);

        if (notCount > 0)
            tv.setText(notCount + "");
        else
            tv.setVisibility(View.INVISIBLE);

        return true;
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

            case android.R.id.home:
                finish();

            case R.id.personal_profile_icon:
                Intent intent = new Intent(GroupActivity.this, MeActivity.class);
                startActivity(intent);

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ExpensesListFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private Float totalAmount;
        private Float debitAmount;
        private Float creditAmount;

        public ExpensesListFragment() {
            totalAmount = new Float(0);
            debitAmount = new Float(0);
            creditAmount = new Float(0);
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ExpensesListFragment newInstance(int sectionNumber) {
            ExpensesListFragment fragment = new ExpensesListFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_expenses, container, false);
            //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            final ProgressBar pBar = (ProgressBar) rootView.findViewById(R.id.pBar);

            final ArrayList<Expense> expenses = new ArrayList<>();

            final RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.expenses_lv);

            ExpensesRecyclerAdapter adapter = new ExpensesRecyclerAdapter(getActivity(), expenses);
            mRecyclerView.setAdapter(adapter);

            LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(getActivity());
            mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(mLinearLayoutManagerVertical);

            /*
            for (int i = 0; i < 16; i++) {
                Expense e = new Expense("Expense " + i, Integer.valueOf(i * i).floatValue(), Currency.getInstance("EUR"),
                        "Description of the expense #" + i + ". Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec luctus fermentum ipsum, non ullamcorper libero rutrum mattis.",
                        null); // Currency string given by ISO 4217
                expenses.add(e);
            }
           */

            LinearLayout meCardViewLayout = (LinearLayout) rootView.findViewById(R.id.credits_cv_ll);
            meCardViewLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), MeActivity.class);
                    startActivity(intent);
                }
            });

            final LinearLayout cards = (LinearLayout) rootView.findViewById(R.id.cards);
            final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
            final int[] previous ={0};
            final boolean[] set = {false};

            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    previous[0]+=dy;
                    if (dy > 0) {
                        fab.hide();
                        if(previous[0] > cards.getHeight()) {
                            cards.animate()
                                    .translationY(0)
                                    .alpha(0.0f)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            cards.setVisibility(View.GONE);
                                            previous[0] = 0;
                                        }
                                    });
                        }
                    }
                    
                    else if (dy < 0) {
                        fab.show();
                        if(previous[0] < -cards.getHeight())
                        cards.animate()
                                .translationY(1)
                                .alpha(1f)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        cards.setVisibility(View.VISIBLE);
                                        previous[0] = 0;
                                    }
                                });
                        }
                }
            });


            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("expenses");
        /*    myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    *//*
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        FirebaseExpense fe = ds.getValue(FirebaseExpense.class);
                        expenses.add(new Expense(fe.getName(),fe.getCost(), Currency.getInstance(Locale.ITALY),fe.getDescription(),null));

                    }*//*

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });*/

            //final boolean[] firsttime = {true};

            myRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    //for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    FirebaseExpense fe = dataSnapshot.getValue(FirebaseExpense.class);
                    fe.setKey(dataSnapshot.getKey());
                    expenses.add(new Expense(fe.getName(),fe.getCost(), Currency.getInstance(Locale.ITALY),fe.getDescription(),null));

                    /* if(firsttime[0]) {
                        ExpensesRecyclerAdapter adapter = new ExpensesRecyclerAdapter(getActivity(), expenses);
                        //mRecyclerView.setAdapter(adapter);

                        LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(getActivity());
                        mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
                        mRecyclerView.setLayoutManager(mLinearLayoutManagerVertical);
                        firsttime[0] = false;
                    }*/

                    //Jured: aggiunto aggiornamento totale nella card Summary
                    // aggiunto aggiornamento barbaro di crediti come se pagassi sempre io
                    //TODO generalizzare l'utilizzo della valuta
                    //TODO calcolo dei miei crediti e debiti con intelligenza
                    totalAmount += Float.valueOf(fe.getCost());
                    TextView totalTextView = (TextView) rootView.findViewById(R.id.expenses_summary_card_tv);
                    totalTextView.setText(Currency.getInstance(Locale.ITALY).getSymbol()+" "+String.format("%.2f", totalAmount));
                    creditAmount += Float.valueOf(fe.getCost());
                    TextView creditTextView = (TextView) rootView.findViewById(R.id.expenses_credit_card_tv);
                    creditTextView.setText(Currency.getInstance(Locale.ITALY).getSymbol()+" "+String.format("%.2f", creditAmount));
                    //debitAmount += Float.valueOf(fe.getCost()); //TO ADD?
                    TextView debitTextView = (TextView) rootView.findViewById(R.id.expenses_debit_card_tv);
                    debitTextView.setText(Currency.getInstance(Locale.ITALY).getSymbol()+" "+String.format("%.2f", debitAmount));

                    pBar.setVisibility(View.GONE);

                    //}

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return rootView;

        }
    }

    public static class ProposalsListFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public ProposalsListFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ProposalsListFragment newInstance(int sectionNumber) {
            ProposalsListFragment fragment = new ProposalsListFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_proposals, container, false);
            //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            ArrayList<Proposal> proposals = new ArrayList<Proposal>();

            for (int i = 0; i < 16; i++) {
                Proposal p = new Proposal("Proposal " + i,
                        "Description of the proposal #" + i + ". Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec luctus fermentum ipsum, non ullamcorper libero rutrum mattis.",
                        Integer.valueOf(i * i).floatValue(), null, Currency.getInstance("EUR"));
                proposals.add(p);
            }

            RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.proposals_rv);
            ProposalsRecyclerAdapter adapter = new ProposalsRecyclerAdapter(getActivity(), proposals);
            mRecyclerView.setAdapter(adapter);

            LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(getActivity());
            mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(mLinearLayoutManagerVertical);

            //RecyclerView expensesList = (RecyclerView) rootView.findViewById(R.id.expenses_lv);
            //expensesList.setAdapter(adapter);

            //final LinearLayout meCardsViewLayout = (LinearLayout) rootView.findViewById(R.id.cards);

            final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

            /*fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Intent i = new Intent(getActivity(), AddExpenseActivity.class);
                    //startActivity(i);
                }
            });*/

            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0)
                        fab.hide();
                    else if (dy < 0)
                        fab.show();
                }
            });

            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0)
                        fab.hide();
                    else if (dy < 0)
                        fab.show();
                }
            });

            return rootView;
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

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
