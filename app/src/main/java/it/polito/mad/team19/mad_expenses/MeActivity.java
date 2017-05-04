package it.polito.mad.team19.mad_expenses;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import it.polito.mad.team19.mad_expenses.Adapters.MeRecyclerAdapter;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;
import it.polito.mad.team19.mad_expenses.Classes.Me;

public class MeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String groupId;
    TextView credito_tv;
    TextView debito_tv;
    ArrayList<Me> me;
    ArrayList<FirebaseGroupMember> groupMembersList = new ArrayList<FirebaseGroupMember>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);
        getSupportActionBar().setTitle(getResources().getString(R.string.personal_profile));
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
        if(uname == null)
            uname = "User";
        else
        if(uname.trim().isEmpty())
            uname = "User";

        me_username_tv.setText(uname);


        //mDatabase = FirebaseDatabase.getInstance().getReference();



        getMembers();



    }

    private void getMembers(){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("gruppi").child(groupId).child("membri");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.e("MembriSnap",dataSnapshot.getValue().toString());
                    groupMembersList.add(new FirebaseGroupMember(child.child("nome").getValue().toString(),null,child.getKey()));
                }

                getBalance();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getBalance() {



        me = new ArrayList<Me>();


        final String myUid = mAuth.getCurrentUser().getUid();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.fromto_rv);
        final MeRecyclerAdapter adapter = new MeRecyclerAdapter(this, me);
        mRecyclerView.setAdapter(adapter);

        LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
        mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManagerVertical);

        final float[] debito = {0};
        final float[] credito = {0};

        final DatabaseReference myRef = database.getReference("utenti").child(myUid).child("bilancio").child(groupId);


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren())
                {
                    float currentBalance = 0;
                    String currentName = child.child("nome").getValue().toString();
                    Log.e("Child",child.child("nome").getValue().toString());
                    for(DataSnapshot child2:  child.child("riepilogo").getChildren())
                    {
                        Log.e("Expense "+child2.getKey().toString(),child2.getValue().toString());
                        currentBalance+=Float.parseFloat(child2.getValue().toString());
                    }

                    if(currentBalance>0)
                        credito[0]+=currentBalance;
                    else
                        debito[0]+=currentBalance;

                    Me e = new Me(currentName, currentBalance, Currency.getInstance("EUR"));
                    me.add(e);

                }
                adapter.notifyDataSetChanged();


                //Ludo: grafico a torta
                PieChart pieChart = (PieChart) findViewById(R.id.chart);

                List<PieEntry> entries = new ArrayList<>();

                if(debito[0]!=0)
                    entries.add(new PieEntry(-debito[0], "Debito"));
                if(credito[0]!=0)
                    entries.add(new PieEntry(credito[0], "Credito"));

                Legend legend = pieChart.getLegend();
                legend.setEnabled(false);

                pieChart.setDescription(null);

                PieDataSet set = new PieDataSet(entries,"Debito/Credito");

                if(debito[0]!=0 && credito[0]!=0)
                    set.setColors(new int[] { R.color.redMaterial, R.color.greenMaterial}, getApplicationContext());
                else
                {
                    if (debito[0] != 0)
                        set.setColors(new int[]{R.color.redMaterial}, getApplicationContext());
                    if (credito[0] != 0)
                        set.setColors(new int[]{R.color.greenMaterial}, getApplicationContext());
                }

                set.setValueTextSize(18);
                set.setValueTextColor(Color.WHITE);
                PieData data = new PieData(set);
                pieChart.setData(data);
                pieChart.invalidate(); // refresh

                if(debito[0]<0)
                    debito_tv.setText(-debito[0]+" €");
                else
                    debito_tv.setText(debito[0]+" €");

                credito_tv.setText(credito[0]+" €");


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

        switch(id)
        {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
               // finish();

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
