package it.polito.mad.team19.mad_expenses;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import java.util.Locale;

import it.polito.mad.team19.mad_expenses.Adapters.ExpenseHistoryAdapter;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseExpense;
import it.polito.mad.team19.mad_expenses.Classes.NetworkChangeReceiver;

public class HistoryPopupActivity extends Activity {

    private FirebaseAuth mAuth;
    private String uid;
    private ListView history_lv;
    private String groupId;
    private String expenseId;

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
        groupId = getIntent().getExtras().getString("groupId");
        expenseId = getIntent().getExtras().getString("expenseId");

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

                //Ludo: ogni volta che si aggiungono elementi alla lista bisogna segnalarlo all'adpater
                expensesHistoryAdapter.notifyDataSetChanged();

                int nHistory = 0;
                for (DataSnapshot expense : dataSnapshot.getChildren()) {

                    FirebaseExpense firebaseExpense = expense.getValue(FirebaseExpense.class);
                    firebaseExpense.setModTime(expense.child("modifyTime").getValue().toString());
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
                FirebaseExpense clicked = expensesHistory.get(position);
                final Intent intent = new Intent (HistoryPopupActivity.this, ExpenseDetailsActivity.class);
                Log.d("Expenses", clicked.toString());
                intent.putExtra("ExpenseName", clicked.getName());
                intent.putExtra("ExpenseImgUrl", clicked.getImage());
                intent.putExtra("ExpenseDesc", clicked.getDescription());
                intent.putExtra("ExpenseCost", String.format(Locale.getDefault(), "%.2f", clicked.getCost()));
                intent.putExtra("ExpenseAuthorId", clicked.getAuthor());
                intent.putExtra("groupId", getIntent().getStringExtra("groupId"));
                intent.putExtra("ExpenseId", clicked.getKey());
                intent.putExtra("isHistoryActivity", "true");
                intent.putExtra("historyId", expenseId);
                intent.putExtra("ExpenseDate", clicked.getDate());
                //intent.putExtra("currentPersonalBalance", String.valueOf(creditAmount - debtAmount));
                //startActivityForResult(intent, EXPENSE_DETAILS);
                startActivity(intent);
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
