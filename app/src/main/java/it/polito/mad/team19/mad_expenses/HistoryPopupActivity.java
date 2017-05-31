package it.polito.mad.team19.mad_expenses;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import it.polito.mad.team19.mad_expenses.Adapters.ExpenseHistoryAdapter;
import it.polito.mad.team19.mad_expenses.Adapters.GroupMembersAdapter;
import it.polito.mad.team19.mad_expenses.Classes.Expense;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseExpense;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;
import it.polito.mad.team19.mad_expenses.Classes.NetworkChangeReceiver;

public class HistoryPopupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String uid;
    private ListView history_lv;

    final ArrayList<FirebaseExpense> expensesHistory = new ArrayList<>();
    final ExpenseHistoryAdapter expensesHistoryAdapter = new ExpenseHistoryAdapter(this, expensesHistory);

    //ArrayList<FirebaseGroupMember> selectedMembers = new ArrayList<>();

    NetworkChangeReceiver netChange;
    IntentFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history_popup);

        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netChange = new NetworkChangeReceiver();
        netChange.setViewForSnackbar(findViewById(android.R.id.content));
        netChange.setDialogShowTrue(false);
        registerReceiver(netChange, filter);


        // Prendi la lista dei membri del gruppo
        String groupId = getIntent().getExtras().getString("groupId");
        String expenseId = getIntent().getExtras().getString("expenseId");

        // Non occupare tutto lo schermo
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .95), (int) (height * .9));


        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference historyRef = database.getReference("storico").child(groupId).child("spese").child(expenseId);
        Log.d("DebugHistoryList"," gruppo " + groupId + " spesa " + expenseId);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        history_lv = (ListView) findViewById(R.id.history_lv);
        history_lv.setAdapter(expensesHistoryAdapter);

        historyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                FirebaseExpense firebaseHistory = dataSnapshot.getValue(FirebaseExpense.class);
                firebaseHistory.setKey(dataSnapshot.getKey());

                expensesHistory.add(firebaseHistory);
                Log.d("DebugHistoryList","spesa trovata: " + firebaseHistory.getKey());

                //Ludo: ogni volta che si aggiungono elementi alla lista bisogna segnalarlo all'adpater
                expensesHistoryAdapter.notifyDataSetChanged();

                int nHistory = 0;
                for (DataSnapshot expense : dataSnapshot.getChildren()) {

                    FirebaseExpense firebaseExpense = expense.getValue(FirebaseExpense.class);
                    firebaseExpense.setKey(expense.getKey());

                    expensesHistory.add(firebaseExpense);
                    Log.d("DebugHistoryList","spesa trovata: " + firebaseExpense.getKey());

                    //Ludo: ogni volta che si aggiungono elementi alla lista bisogna segnalarlo all'adpater
                    expensesHistoryAdapter.notifyDataSetChanged();

                    nHistory++;
                }

                history_lv.invalidate();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ContributorsPopup", "Could not read group members");
            }
        });

        history_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

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

}
