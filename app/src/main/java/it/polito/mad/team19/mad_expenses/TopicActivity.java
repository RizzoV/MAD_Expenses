package it.polito.mad.team19.mad_expenses;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Adapters.TopicAdapter;
import it.polito.mad.team19.mad_expenses.Classes.Topic;

public class TopicActivity extends AppCompatActivity {

    ListView msg_lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

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
}
