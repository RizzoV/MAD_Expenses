package it.polito.mad.team19.mad_expenses;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
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
import it.polito.mad.team19.mad_expenses.Classes.Expense;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseExpense;
import it.polito.mad.team19.mad_expenses.Classes.Me;

/**
 * Created by Valentino on 22/05/2017.
 */

public class ExpensesListFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private Float totalAmount;
    private Float debtAmount;
    private Float creditAmount;
    private Activity mActivity;

    private HashMap<String, Me> balancesMap = new HashMap<>();
    private int times = 0;
    private boolean free = true;

    private SwipeRefreshLayout expenseSwipe;

    private static final int EXPENSE_DETAILS = 5;

    TextView noExpenses_tv;
    String groupId;
    String myUid;
    TextView creditTextView;
    TextView debitTextView;
    TextView totalTextView;
    ExpensesRecyclerAdapter expensesListAdapter;
    ProgressBar pBar;
    LinearLayout cards;
    LinearLayout containerExpenses;
    FloatingActionButton fab;
    ArrayList<Expense> expenses;

    public ExpensesListFragment() {
        totalAmount = (float) 0;
        debtAmount = (float) 0;
        creditAmount = (float) 0;
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

        pBar = (ProgressBar) rootView.findViewById(R.id.pBar);

        expenses = new ArrayList<>();

        final RecyclerView expensesListRecyclerView = (RecyclerView) rootView.findViewById(R.id.expenses_lv);
        expensesListAdapter = new ExpensesRecyclerAdapter(getActivity(), expenses,getActivity().getIntent().getStringExtra("groupId") );
        expensesListRecyclerView.setAdapter(expensesListAdapter);

        LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(getActivity());
        mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
        expensesListRecyclerView.setLayoutManager(mLinearLayoutManagerVertical);

        containerExpenses = (LinearLayout) rootView.findViewById(R.id.frag_expenses_upper_ll);

        expenseSwipe = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshExpense);
        expenseSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });


        expensesListAdapter.SetOnItemClickListener(new ExpensesRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Expense clicked = expenses.get(position);
                final Intent intent = new Intent(getActivity(), ExpenseDetailsActivity.class);
                Log.d("Expenses", clicked.toString());
                intent.putExtra("ExpenseName", clicked.getName());
                intent.putExtra("ExpenseImgUrl", clicked.getImagelink());
                intent.putExtra("ExpenseDesc", clicked.getDescritpion());
                intent.putExtra("ExpenseCost", String.format(Locale.getDefault(), "%.2f", clicked.getCost()));
                intent.putExtra("ExpenseAuthorId", clicked.getAuthor());
                intent.putExtra("groupId", getActivity().getIntent().getStringExtra("groupId"));
                intent.putExtra("ExpenseId", clicked.getFirebaseId());
                intent.putExtra("currentPersonalBalance", String.valueOf(creditAmount - debtAmount));
                startActivityForResult(intent, EXPENSE_DETAILS);
            }
        });

        expensesListAdapter.SetOnItemLongClickListener(new ExpensesRecyclerAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                //aggiungere eventuale dialog per opzioni sulle spese
            }
        });


        LinearLayout meCardViewLayout = (LinearLayout) rootView.findViewById(R.id.credits_cv_ll);
        meCardViewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MeActivity.class);
                intent.putExtra("groupId", getActivity().getIntent().getStringExtra("groupId"));
                    /* VALE
                     * Crea un bundle contenente le informazioni su spese per non doverle riscaricare
                     * nella activity sui dettagli del gruppo
                     */
                Bundle b = new Bundle();
                ArrayList<Me> balancesArray = new ArrayList<>();
                for (Me currentProfile : balancesMap.values())
                    balancesArray.add(currentProfile);

                b.putParcelableArrayList("balancesArray", balancesArray);
                intent.putExtra("balancesBundle", b);
                startActivity(intent);
            }
        });

        cards = (LinearLayout) rootView.findViewById(R.id.cards);
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        final int[] previous = {0};
        final boolean[] set = {false};

        expensesListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        noExpenses_tv = (TextView) rootView.findViewById(R.id.noexpenses_tv);
        groupId = getActivity().getIntent().getStringExtra("groupId");
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        creditTextView = (TextView) rootView.findViewById(R.id.expenses_credit_card_tv);
        debitTextView = (TextView) rootView.findViewById(R.id.expenses_debit_card_tv);
        totalTextView = (TextView) rootView.findViewById(R.id.expenses_summary_total_amount_tv);


        refreshList();

        return rootView;
    }

    public void refreshList()
    {

            /* VALE
             * Calcola statistiche su credito, debito e totale.
             * Raccogli anche informazioni su crediti e debiti verso gli altri utenti già che ci sei,
             * mettendole in balancesMap che sarà poi usata per passare queste informazioni al profilo
             * personale senza riscaricare tutto
             */

        containerExpenses.setVisibility(View.INVISIBLE);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("gruppi").child(groupId).child("expenses");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                totalAmount = (float) 0;
                creditAmount = (float) 0;
                debtAmount = (float) 0;
                expenses.clear();
                balancesMap.clear();





                if (dataSnapshot.hasChildren() && free) {
                    noExpenses_tv.setVisibility(View.GONE);

                    for (DataSnapshot expense : dataSnapshot.getChildren()) {

                        FirebaseExpense firebaseExpense = expense.getValue(FirebaseExpense.class);
                        firebaseExpense.setKey(expense.getKey());
                        expenses.add(0, new Expense(firebaseExpense.getName(), firebaseExpense.getCost(), Currency.getInstance(Locale.ITALY), firebaseExpense.getDescription(), firebaseExpense.getImage(), firebaseExpense.getAuthor(), expense.getKey()));

                        //Ludo: ogni volta che si aggiungono elementi alla lista bisogna segnalarlo all'adpater
                        expensesListAdapter.notifyDataSetChanged();

                        //TODO generalizzare l'utilizzo della valuta
                        totalAmount += firebaseExpense.getCost();

                        DataSnapshot meRef = expense.child("contributors").child(myUid);
                        if (meRef.exists()) {
                            // Sono un contributor
                            for (DataSnapshot expenseBalance : meRef.child("riepilogo").getChildren()) {

                                if (balancesMap.containsKey(expenseBalance.getKey())) {
                                    if (expenseBalance.child("amount").exists())
                                        balancesMap.get(expenseBalance.getKey()).addPartialAmount(Float.parseFloat(expenseBalance.child("amount").getValue().toString()));
                                } else {
                                    if (expenseBalance.child("amount").exists() && expenseBalance.child("nome").exists()) {
                                        Me newDebtor;

                                        if (expenseBalance.child("immagine").exists())
                                            newDebtor = new Me(expenseBalance.getKey(), expenseBalance.child("nome").getValue().toString(), Float.parseFloat(expenseBalance.child("amount").getValue().toString()), Currency.getInstance("EUR"), expenseBalance.child("immagine").getValue().toString());
                                        else
                                            newDebtor = new Me(expenseBalance.getKey(), expenseBalance.child("nome").getValue().toString(), Float.parseFloat(expenseBalance.child("amount").getValue().toString()), Currency.getInstance("EUR"), null);

                                        balancesMap.put(expenseBalance.getKey(), newDebtor);
                                    }
                                }
                            }
                        } else {
                            meRef = expense.child("debtors").child(myUid);
                            if (meRef.exists()) {
                                // Sono un debtor
                                for (DataSnapshot expenseBalance : meRef.child("riepilogo").getChildren()) {
                                    if (balancesMap.containsKey(expenseBalance.getKey())) {
                                        if (expenseBalance.child("amount").exists())
                                            balancesMap.get(expenseBalance.getKey()).addPartialAmount(Float.parseFloat(expenseBalance.child("amount").getValue().toString()));
                                    } else {
                                        if (expenseBalance.child("amount").exists() && expenseBalance.child("nome").exists()) {
                                            Me newDebtor;
                                            if (expenseBalance.child("immagine").exists())
                                                newDebtor = new Me(expenseBalance.getKey(), expenseBalance.child("nome").getValue().toString(), Float.parseFloat(expenseBalance.child("amount").getValue().toString()), Currency.getInstance("EUR"), expenseBalance.child("immagine").getValue().toString());
                                            else
                                                newDebtor = new Me(expenseBalance.getKey(), expenseBalance.child("nome").getValue().toString(), Float.parseFloat(expenseBalance.child("amount").getValue().toString()), Currency.getInstance("EUR"), null);
                                            balancesMap.put(expenseBalance.getKey(), newDebtor);

                                        }
                                    }
                                }
                            }
                        }
                    }


                    for (Me me : balancesMap.values()) {
                        float currentAmount = me.getAmount();
                        if (currentAmount > 0)
                            creditAmount += currentAmount;
                        else
                            debtAmount += currentAmount;
                    }

                    debtAmount = Math.abs(debtAmount);
                    creditTextView.setText(String.format(Locale.getDefault(), "%.2f", creditAmount) + " " + Currency.getInstance(Locale.ITALY).getSymbol());
                    debitTextView.setText(String.format(Locale.getDefault(), "%.2f", debtAmount) + " " + Currency.getInstance(Locale.ITALY).getSymbol());
                    totalTextView.setText(String.format(Locale.getDefault(), "%.2f", totalAmount) + " " + Currency.getInstance(Locale.ITALY).getSymbol());

                    database.getReference("utenti").child(myUid).child("gruppi").child(groupId).child("credito").setValue(creditAmount);
                    database.getReference("utenti").child(myUid).child("gruppi").child(groupId).child("debito").setValue(debtAmount);

                    pBar.setVisibility(View.GONE);

                } else {
                    pBar.setVisibility(View.GONE);
                    noExpenses_tv.setVisibility(View.VISIBLE);
                }

                expenseSwipe.setRefreshing(false);
                containerExpenses.setVisibility(View.VISIBLE);


                ((GroupActivity) mActivity).passBalancesArray(balancesMap.values());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ExpensesFragment", "Could not retrieve the list of expenses");
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivity = (Activity) context;
    }
}
