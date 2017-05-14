package it.polito.mad.team19.mad_expenses;

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
            Log.e("Receiver", "register on resum");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (netChange != null) {
            netChange.closeSnack();
            unregisterReceiver(netChange);
            netChange = null;
            Log.e("Receiver", "unregister on pause");
        }

    }
}
