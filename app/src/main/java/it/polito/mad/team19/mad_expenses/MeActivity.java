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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import it.polito.mad.team19.mad_expenses.Adapters.MeRecyclerAdapter;
import it.polito.mad.team19.mad_expenses.Classes.Me;

public class MeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);
        getSupportActionBar().setTitle(getResources().getString(R.string.personal_profile));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        if(uname.trim().isEmpty() || uname == null)
            uname = "User";

        me_username_tv.setText(uname);


        //mDatabase = FirebaseDatabase.getInstance().getReference();

        //Ludo: grafico a torta
        PieChart pieChart = (PieChart) findViewById(R.id.chart);

        List<PieEntry> entries = new ArrayList<>();

        entries.add(new PieEntry(150f, "Debito"));
        entries.add(new PieEntry(300f, "Credito"));

        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

        pieChart.setDescription(null);

        PieDataSet set = new PieDataSet(entries,"Debito/Credito");
        set.setColors(new int[] { R.color.redMaterial, R.color.greenMaterial}, this);
        set.setValueTextSize(18);
        set.setValueTextColor(Color.WHITE);
        PieData data = new PieData(set);
        pieChart.setData(data);
        pieChart.invalidate(); // refresh


        ArrayList<Me> me = new ArrayList<Me>();

        for(int i = 0; i<4; i++) {
            Me e = new Me("Nome", Integer.valueOf(1000).floatValue(), Currency.getInstance("EUR"));
            me.add(e);
        }

        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.fromto_rv);
        MeRecyclerAdapter adapter = new MeRecyclerAdapter(this, me);
        mRecyclerView.setAdapter(adapter);

        LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
        mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManagerVertical);

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
