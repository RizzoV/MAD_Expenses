package it.polito.mad.team19.mad_expenses;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Adapters.MeRecyclerAdapter;
import it.polito.mad.team19.mad_expenses.Classes.Me;

public class MeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);
        getSupportActionBar().setTitle(getResources().getString(R.string.personal_profile));
        getSupportActionBar().setHomeButtonEnabled(true);

        ArrayList<Me> me = new ArrayList<Me>();


        for(int i = 0; i<4; i++) {
            Me e = new Me("Nome", Integer.valueOf(1000).floatValue());
            me.add(e);
        }

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.fromto_rv);
        MeRecyclerAdapter adapter = new MeRecyclerAdapter(this, me);
        mRecyclerView.setAdapter(adapter);

        LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
        mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManagerVertical);

    }
}
