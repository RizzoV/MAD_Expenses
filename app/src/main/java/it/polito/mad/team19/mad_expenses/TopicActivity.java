package it.polito.mad.team19.mad_expenses;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Adapters.TopicAdapter;
import it.polito.mad.team19.mad_expenses.Classes.NetworkChangeReceiver;
import it.polito.mad.team19.mad_expenses.Classes.Topic;

public class TopicActivity extends AppCompatActivity {

    ListView msg_lv;

    NetworkChangeReceiver netChange;
    IntentFilter filter;
    String topicType;
    String tid;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netChange = new NetworkChangeReceiver();
        netChange.setViewForSnackbar(findViewById(android.R.id.content));
        netChange.setDialogShowTrue(false);
        registerReceiver(netChange, filter);

        topicType = getIntent().getStringExtra("topicType");
        tid = getIntent().getStringExtra("topicId");
        name = getIntent().getStringExtra("topicName");


        Log.d("Topic","type: "+topicType);
        Log.d("Topic","id: "+tid);

        getSupportActionBar().setTitle("Discussione - " +name);

        msg_lv = (ListView) findViewById(R.id.messagesContainer);
        ArrayList<Topic> msgList = new ArrayList<Topic>();

        for(int i=0;i<20;i++)
        {
            if(i%2==0)
                msgList.add(new Topic("Bbbolz","Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",true));
            else
                msgList.add(new Topic("Jureeee","Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",false));

        }

        TopicAdapter adapter = new TopicAdapter(this,msgList);
        msg_lv.setAdapter(adapter);
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

    @Override
    public Intent getSupportParentActivityIntent() {
        if (getIntent().getStringExtra("ProposalInfoIntent") != null) {
            return getParentActivityIntentImpl();
        } else return super.getSupportParentActivityIntent();
    }

    @Override
    public Intent getParentActivityIntent() {
        if (getIntent().getStringExtra("ProposalInfoIntent") != null) {
            return getParentActivityIntentImpl();
        } else return super.getParentActivityIntent();
    }

    private Intent getParentActivityIntentImpl() {
        Intent i = null;

        // Here you need to do some logic to determine from which Activity you came.
        // example: you could pass a variable through your Intent extras and check that.
        i = new Intent(this, ProposalDetailsActivity.class);
        // set any flags or extras that you need.
        // If you are reusing the previous Activity (i.e. bringing it to the top
        // without re-creating a new instance) set these flags:
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // if you are re-using the parent Activity you may not need to set any extras
        //i.putExtra("someExtra", "whateverYouNeed");
        return i;
    }
}
