package it.polito.mad.team19.mad_expenses;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import it.polito.mad.team19.mad_expenses.Adapters.MeRecyclerAdapter;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;
import it.polito.mad.team19.mad_expenses.Classes.Me;

public class MeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String groupId;
    TextView credito_tv;
    TextView debito_tv;
    ArrayList<Me> me = new ArrayList<>();
    ArrayList<FirebaseGroupMember> groupMembersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);
        getSupportActionBar().setTitle(getResources().getString(R.string.personal_balance));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupId = getIntent().getStringExtra("groupId");

        credito_tv = (TextView) findViewById(R.id.credito_tv);
        debito_tv = (TextView) findViewById(R.id.debito_tv);

        final ScrollView sw = (ScrollView) findViewById(R.id.scrollView);

        sw.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                sw.post(new Runnable() {
                    public void run() {
                        sw.fullScroll(View.FOCUS_UP);
                    }
                });
            }
        });

        TextView me_username_tv = (TextView) findViewById(R.id.me_username_tv);

        //Ludo: informazioni utente da fb

        mAuth = FirebaseAuth.getInstance();

        String uname = mAuth.getCurrentUser().getDisplayName();
        if (uname == null)
            uname = "User";
        else if (uname.trim().isEmpty())
            uname = "User";

        me_username_tv.setText(uname);

        getMembers();
    }

    private void getMembers() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("gruppi").child(groupId).child("membri");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.d("MembriSnap", dataSnapshot.getValue().toString());
                    groupMembersList.add(new FirebaseGroupMember(child.child("nome").getValue().toString(), null, child.getKey()));
                }

                getBalance();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getBalance() {

        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.fromto_rv);
        final MeRecyclerAdapter adapter = new MeRecyclerAdapter(this, me);
        mRecyclerView.setAdapter(adapter);

        LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
        mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManagerVertical);

        float debito = 0;
        float credito = 0;

        /* VALE
         * Prendi le informazioni dal Bundle passato tramite l'intent
         */

        ArrayList<Me> balancesArray = getIntent().getBundleExtra("balancesBundle").getParcelableArrayList("balancesArray");

        if(balancesArray == null) {
            Log.e("MeActivity", "balancesArray è NULL");
            return;
        }

        for (Me otherMember : balancesArray) {
            me.add(otherMember);
            if (otherMember.getAmount() > 0)
                credito += otherMember.getAmount();
            else
                debito += otherMember.getAmount();
        }

        adapter.notifyDataSetChanged();

        //Ludo: grafico a torta
        PieChart pieChart = (PieChart) findViewById(R.id.chart);

        List<PieEntry> entries = new ArrayList<>();

        if (debito != 0)
            entries.add(new PieEntry(-debito, "Debito"));
        if (credito != 0)
            entries.add(new PieEntry(credito, "Credito"));

        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

        pieChart.setDescription(null);

        PieDataSet set = new PieDataSet(entries, "Debito/Credito");

        if (debito != 0 && credito != 0)
            set.setColors(new int[] {
                            R.color.redMaterial, R.color.textGreen
            },
           getApplicationContext());
        else {
            if (debito != 0)
                set.setColors(new int[]{R.color.redMaterial}, getApplicationContext());
            if (credito != 0)
                set.setColors(new int[]{R.color.textGreen}, getApplicationContext());
        }

        set.setValueTextSize(18);
        set.setValueTextColor(Color.WHITE);
        PieData data = new PieData(set);
        pieChart.setData(data);
        pieChart.invalidate(); // refresh

        if (debito < 0)
            debito = -debito;
        debito_tv.setText(String.format(Locale.getDefault(), "%.2f", debito) + " " + Currency.getInstance(Locale.ITALY).getSymbol());

        credito_tv.setText(String.format(Locale.getDefault(), "%.2f", credito) + " " + Currency.getInstance(Locale.ITALY).getSymbol());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
