package it.polito.mad.team19.mad_expenses;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
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

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import it.polito.mad.team19.mad_expenses.Adapters.ExpensesRecyclerAdapter;
import it.polito.mad.team19.mad_expenses.Adapters.ProposalsRecyclerAdapter;
import it.polito.mad.team19.mad_expenses.Classes.Expense;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseExpense;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseProposal;
import it.polito.mad.team19.mad_expenses.Classes.Proposal;

import static java.lang.Thread.sleep;

enum TabsList {
    EXPENSES,
    PROPOSALS
}

public class GroupActivity extends AppCompatActivity {

    private static final int REQUEST_INVITE = 66;
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

    private String groupName;
    private String groupId;
    private String groupImageUrl;
    TabsList selectedTab;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    final ArrayList<FirebaseGroupMember> groupMembersList = new ArrayList<FirebaseGroupMember>();

    ProgressDialog barProgressDialog;

    protected static final String TAG = "firebaseAuth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);


        // Get the intent which has started this activity
        Intent intent = getIntent();

        // Set the activity name retrieving it by the extras of the intent
        groupName = intent.getStringExtra("groupName");
        groupId = intent.getStringExtra("groupId");
        groupImageUrl = intent.getStringExtra("groupImage");
        setTitle(groupName);

        // Initially the displayed tab will be the EXPENSES one
        selectedTab = TabsList.EXPENSES;

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Manage group image
        if (groupImageUrl != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReferenceFromUrl(groupImageUrl);
            final long ONE_MEGABYTE = 1024 * 1024;
            storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(getCircleBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length)), convertDipToPixels(40), convertDipToPixels(40), true));
                    toolbar.setLogo(d);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    //TODO: Handle any errors
                }
            });
        } else {
            Drawable d = getResources().getDrawable(R.mipmap.ic_group);
            d.setBounds(0, 0, convertDipToPixels(40), convertDipToPixels(40));
            toolbar.setLogo(d);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                startActivity(intent);
                Log.e("BolzDebug", "mannaiaBBolz");
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
                int resultCode;
                switch (selectedTab) {
                    case EXPENSES:
                        i = new Intent(GroupActivity.this, AddExpenseActivity.class);
                        resultCode = 666;
                        i.putExtra("groupId", groupId);
                        break;
                    case PROPOSALS:
                        i = new Intent(GroupActivity.this, AddProposalActivity.class);
                        resultCode = 6;
                        i.putExtra("groupId", groupId);
                        break;
                    default:
                        i = new Intent(GroupActivity.this, AddExpenseActivity.class);
                        resultCode = 666;
                        break;
                }

                startActivityForResult(i, resultCode);
            }
        });
    }

    public int convertDipToPixels(float dips) {
        return (int) (dips * getApplicationContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
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

        // Retrieve the notification count textView from the menu
        MenuItem item = menu.findItem(R.id.notifications_icon);
        MenuItemCompat.setActionView(item, R.layout.notifications_ab_layout);
        RelativeLayout notifCount = (RelativeLayout) MenuItemCompat.getActionView(item);

        barProgressDialog = new ProgressDialog(GroupActivity.this,R.style.notifications_dialog){
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.dialog_notifications);
                getWindow().setLayout(AppBarLayout.LayoutParams.MATCH_PARENT,
                        AppBarLayout.LayoutParams.MATCH_PARENT);
            }
        };

        barProgressDialog.setCancelable(true);


        notifCount.findViewById(R.id.notifications_icon_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Clicked","click");
                barProgressDialog.show();
            }
        });


        // Set up a listener which is able to get the number of notifications for the user in this group
        final TextView tv = (TextView) notifCount.findViewById(R.id.counter);
        ImageView im = (ImageView) notifCount.findViewById(R.id.notifications_icon_action);
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("utenti").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("gruppi").child(groupId).child("notifiche");
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int notificationsCount = dataSnapshot.getValue(Integer.class);
                if (notificationsCount > 0) {
                    tv.setText(String.valueOf(notificationsCount));
                    tv.setVisibility(View.VISIBLE);
                } else {
                    tv.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Notifications listener - The read failed: " + databaseError.getCode());
            }
        });
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

        if (requestCode == 666 && resultCode == RESULT_OK) {
            Log.e("ExpenseIDActivity", data.getStringExtra("expenseId").toString());
            if (groupMembersList.size() > 0)
                groupMembersList.clear();

            ArrayList<FirebaseGroupMember> contributors = data.getParcelableArrayListExtra("contributors");
            ArrayList<FirebaseGroupMember> excluded = data.getParcelableArrayListExtra("excluded");

            //TODO: far avviare tutto in un thread
            getMembers(data.getStringExtra("expenseId").toString(), Float.parseFloat(data.getStringExtra("expenseTotal")), data.getStringExtra("expenseUId"),
                    data.getStringExtra("expenseUserName"), contributors, excluded);
        }
    }

    private void getMembers(final String expenseId, final float expenseTotal, final String expenseUuid, final String expenseUserName,
                            final ArrayList<FirebaseGroupMember> contributors, final ArrayList<FirebaseGroupMember> excluded) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("gruppi").child(groupId).child("membri");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.e("MembriSnap", dataSnapshot.getValue().toString());
                    groupMembersList.add(new FirebaseGroupMember(child.child("nome").getValue().toString(), null, child.getKey()));
                }

                setBalance(expenseId, expenseTotal, expenseUuid, expenseUserName, contributors, excluded);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("GroupActivity", "Unable to read group members");
            }
        });
    }

    private void setBalance(String idExpense, float expenseTotal, String expenseUserUid, String expenseUserName, ArrayList<FirebaseGroupMember> contributors, ArrayList<FirebaseGroupMember> excluded) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
    /*
        for (int i = 0; i < groupMembersList.size(); i++) {
            final FirebaseGroupMember currentMember;
            currentMember = groupMembersList.get(i);
            //Ludo: se sono chi ha sostenuto la spesa
            if (expenseUserUid.equals(currentMember.getUid())) {
                //Ludo: devo aggiungere tutti i crediti escludendo me stesso
                for (int j = 0; j < groupMembersList.size(); j++) {
                    final FirebaseGroupMember temp = groupMembersList.get(j);
                    if (!temp.getUid().equals(expenseUserUid)) {
                        final DatabaseReference myRef = database.getReference("utenti").child(currentMember.getUid()).child("bilancio").child(groupId).child(temp.getUid());
                        myRef.child("riepilogo").child(idExpense).setValue(String.format("%.2f", expenseTotal / groupMembersList.size()));
                        myRef.child("nome").setValue(temp.getName());
                    }
                }
            } else {
                //Ludo: se invece non sono chi ha sostenuto la spesa ho un debito verso chi l'ha sostenuta
                final DatabaseReference myRef = database.getReference("utenti").child(currentMember.getUid()).child("bilancio").child(groupId).child(expenseUserUid);
                myRef.child("riepilogo").child(idExpense).setValue(expenseTotal / groupMembersList.size());
                myRef.child("nome").setValue(expenseUserName);

            }
        }

   */
        for (FirebaseGroupMember groupMember : groupMembersList) {
            Boolean stop = Boolean.FALSE;
            for (FirebaseGroupMember excludedMember : excluded) {
                if (groupMember.getUid().equals(excludedMember.getUid())) {
                    stop = Boolean.TRUE;
                    break;
                }
            }

            for (FirebaseGroupMember contributor : contributors) {
                if (groupMember.getUid().equals(contributor.getUid())) {
                    stop = Boolean.TRUE;
                    break;
                }
            }

            if (stop)
                continue;

            for (FirebaseGroupMember contributor : contributors) {
                DatabaseReference debtorRef = database.getReference("utenti").child(groupMember.getUid()).child("bilancio").child(groupId).child(contributor.getUid());
                debtorRef.child("riepilogo").child(idExpense).setValue(String.format("%.2f", -(expenseTotal / contributors.size() / (groupMembersList.size() - excluded.size()))).replace(",","."));
                debtorRef.child("nome").setValue(contributor.getName());

                DatabaseReference creditorRef = database.getReference("utenti").child(contributor.getUid()).child("bilancio").child(groupId).child(groupMember.getUid());
                creditorRef.child("riepilogo").child(idExpense).setValue(String.format("%.2f", +(expenseTotal / contributors.size() / (groupMembersList.size() - excluded.size()))).replace(",","."));
                creditorRef.child("nome").setValue(groupMember.getName());
            }

        }
    }

    private void onInviteClicked() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link) + "/" + groupId))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }


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
            final ExpensesRecyclerAdapter adapter = new ExpensesRecyclerAdapter(getActivity(), expenses);
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

            adapter.SetOnItemClickListener(new ExpensesRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Expense clicked = expenses.get(position);
                    final Intent intent = new Intent(getActivity(), ExpenseDetailsActivity.class);
                    Log.e("Expenses", clicked.toString());
                    intent.putExtra("ExpenseName", clicked.getName());
                    intent.putExtra("ExpenseImgUrl", clicked.getImagelink());
                    intent.putExtra("ExpenseDesc", clicked.getDescritpion());
                    intent.putExtra("ExpenseCost", clicked.getCost().toString());
                    intent.putExtra("ExpenseAuthorId", clicked.getAuthor());
                    intent.putExtra("groupId", getActivity().getIntent().getStringExtra("groupId"));
                    intent.putExtra("ExpenseId", clicked.getFirebaseId());
                    startActivity(intent);
                }
            });

            adapter.SetOnItemLongClickListener(new ExpensesRecyclerAdapter.OnItemLongClickListener() {
                @Override
                public void onItemLongClick(View view, int position) {
                    Log.e("DebugExpenseLongClicked", position + "item clicked");
                    DialogFragment newFragment = new GalleryOrCameraDialog();
                    newFragment.show(getActivity().getSupportFragmentManager(), "imageDialog");
                }
            });


            LinearLayout meCardViewLayout = (LinearLayout) rootView.findViewById(R.id.credits_cv_ll);
            meCardViewLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), MeActivity.class);
                    intent.putExtra("groupId", getActivity().getIntent().getStringExtra("groupId"));
                    startActivity(intent);
                }
            });

            final LinearLayout cards = (LinearLayout) rootView.findViewById(R.id.cards);
            final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
            final int[] previous = {0};
            final boolean[] set = {false};

            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    previous[0] += dy;
                    if (dy > 0) {
                        fab.hide();
                        if (previous[0] > cards.getHeight()) {
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
                    } else if (dy < 0) {
                        fab.show();
                        if (previous[0] < -cards.getHeight())
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


            final TextView noexpensestv = (TextView) rootView.findViewById(R.id.noexpenses_tv);

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("gruppi").child(getActivity().getIntent().getStringExtra("groupId")).child("expenses");


            myRef.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    totalAmount = Float.valueOf(0);
                    creditAmount = Float.valueOf(0);
                    if (dataSnapshot.hasChildren()) {
                        noexpensestv.setVisibility(View.GONE);
                        //Ludo: ogni volta che si ricrea la lista, prima bisogna svuotarla per non avere elementi doppi
                        expenses.clear();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            FirebaseExpense fe = child.getValue(FirebaseExpense.class);
                            fe.setKey(child.getKey());
                            expenses.add(new Expense(fe.getName(), fe.getCost(), Currency.getInstance(Locale.ITALY), fe.getDescription(), fe.getImage(), fe.getAuthor(), child.getKey()));
                            //Ludo: ogni volta che si aggiungono elementi alla lista bisogna segnalarlo all'adpater
                            adapter.notifyDataSetChanged();

                            //TODO generalizzare l'utilizzo della valuta
                            //TODO calcolo dei miei crediti e debiti con intelligenza
                            totalAmount += Float.valueOf(fe.getCost());
                            TextView totalTextView = (TextView) rootView.findViewById(R.id.expenses_summary_card_tv);
                            totalTextView.setText(Currency.getInstance(Locale.ITALY).getSymbol() + " " + String.format("%.2f", totalAmount));

                            TextView creditDebitTextView_amount = (TextView) rootView.findViewById(R.id.expenses_credit_debit_card_tv);
                            TextView creditDebitTextView_title = (TextView) rootView.findViewById(R.id.expenses_credit_debit_title_tv);

                            float myCreditDebitAmount = 0;

                         /*   if(myCreditDebitAmount>0)
                            {
                                creditDebitTextView_amount.setTextColor(ContextCompat.getColor(getContext(), R.color.greenMaterial));
                                creditDebitTextView_title.setText(R.string.credit);
                            }
                            else
                            {
                                creditDebitTextView_amount.setTextColor(ContextCompat.getColor(getContext(), R.color.redMaterial));
                                creditDebitTextView_title.setText(R.string.debit);
                            }*/

                            creditDebitTextView_amount.setText(Currency.getInstance(Locale.ITALY).getSymbol() + " " + String.format("%.2f", myCreditDebitAmount));

                            //debitAmount += Float.valueOf(fe.getCost()); //TO ADD?
                          /*  TextView debitTextView = (TextView) rootView.findViewById(R.id.expenses_debit_card_tv);
                            debitTextView.setText(Currency.getInstance(Locale.ITALY).getSymbol() + " " + String.format("%.2f", debitAmount));*/

                            pBar.setVisibility(View.GONE);
                        }
                    } else {
                        pBar.setVisibility(View.GONE);
                        noexpensestv.setVisibility(View.VISIBLE);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return rootView;

        }


        void getDataFromFirebase() {

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

            final ArrayList<Proposal> proposals = new ArrayList<Proposal>();

            RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.proposals_rv);
            final ProposalsRecyclerAdapter adapter = new ProposalsRecyclerAdapter(getActivity(), proposals);
            mRecyclerView.setAdapter(adapter);

            LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(getActivity());
            mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(mLinearLayoutManagerVertical);


        /*    for (int i = 0; i < 16; i++) {
                Proposal p = new Proposal("Proposal " + i,
                        "Description of the proposal #" + i + ". Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec luctus fermentum ipsum, non ullamcorper libero rutrum mattis.",
                        Integer.valueOf(i * i).floatValue(), null, Currency.getInstance("EUR"));
                proposals.add(p);
            }*/

            final TextView noproposalstv = (TextView) rootView.findViewById(R.id.noproposals_tv);


            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("gruppi").child(getActivity().getIntent().getStringExtra("groupId")).child("proposals");


            myRef.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        noproposalstv.setVisibility(View.GONE);
                        //Ludo: ogni volta che si ricrea la lista, prima bisogna svuotarla per non avere elementi doppi
                        proposals.clear();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            FirebaseProposal fp = child.getValue(FirebaseProposal.class);
                            proposals.add(new Proposal(fp.getName(), fp.getDescription(), fp.getCost(), null, Currency.getInstance("EUR")));
                            //Ludo: ogni volta che si aggiungono elementi alla lista bisogna segnalarlo all'adpater
                            adapter.notifyDataSetChanged();


                            //pBar.setVisibility(View.GONE);
                        }
                    } else {
                        //pBar.setVisibility(View.GONE);
                        noproposalstv.setVisibility(View.VISIBLE);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


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
