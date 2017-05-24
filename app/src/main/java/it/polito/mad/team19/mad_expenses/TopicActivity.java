package it.polito.mad.team19.mad_expenses;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import it.polito.mad.team19.mad_expenses.Adapters.TopicAdapter;
import it.polito.mad.team19.mad_expenses.Classes.NetworkChangeReceiver;
import it.polito.mad.team19.mad_expenses.Classes.Topic;

import static it.polito.mad.team19.mad_expenses.R.id.chains;
import static it.polito.mad.team19.mad_expenses.R.id.msg;

public class TopicActivity extends AppCompatActivity {

    ListView msg_lv;

    NetworkChangeReceiver netChange;
    IntentFilter filter;
    String tid;
    String name;
    String topicType;
    String expenseId;
    String proposalId;

    private FirebaseAuth mAuth;
    private String usrId;
    private String groupId;

    EditText messageEditText;

    TopicAdapter adapter;
    private HashMap<String, Integer> topicMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        mAuth = FirebaseAuth.getInstance(); //Firebase Bolz
        
        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netChange = new NetworkChangeReceiver();
        netChange.setViewForSnackbar(findViewById(android.R.id.content));
        netChange.setDialogShowTrue(false);
        registerReceiver(netChange, filter);

        topicType = getIntent().getStringExtra("topicType");
        name = getIntent().getStringExtra("topicName");

        Log.d("Topic","type: "+topicType);
        Log.d("Topic","id: "+tid);

        getSupportActionBar().setTitle("Discussione - " +name);

        msg_lv = (ListView) findViewById(R.id.messagesContainer);
        final ArrayList<Topic> msgList = new ArrayList<Topic>();

        groupId = getIntent().getStringExtra("groupId");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference newMessageRef;

        if (topicType.equals("expenses"))
        {
            expenseId = getIntent().getStringExtra("expenseId");
            newMessageRef = database.getReference().child("gruppi").child(groupId).child("expenses").child(expenseId).child("topic").child("messages");
        }
        else
            {
            proposalId = getIntent().getStringExtra("proposalId");
            newMessageRef = database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("topic").child("messages");
            }
        usrId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        newMessageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                {
                    for(DataSnapshot topic:dataSnapshot.getChildren())
                    {
                        if(topic.child("date").getValue()!=null && topic.child("name").getValue()!=null && topic.child("text").getValue()!=null && topic.child("uid").getValue()!=null )
                        {
                            if(topicMap.get(topic.getKey()) == null)
                            {
                                topicMap.put(topic.getKey(), 69);

                            if(!topic.child("uid").getValue().toString().equals(usrId))
                                msgList.add(new Topic(topic.child("name").getValue().toString(), topic.child("text").getValue().toString(),topic.child("date").getValue().toString(), false));
                            else
                                msgList.add(new Topic(topic.child("name").getValue().toString(), topic.child("text").getValue().toString(),topic.child("date").getValue().toString(), true));

                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        messageEditText = (EditText) findViewById(R.id.messageEdit);
        Button send_btn = (Button) findViewById(R.id.chatSendButton);
        send_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if(!messageEditText.getText().toString().trim().isEmpty())
                {
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");


                    String uuid = newMessageRef.push().getKey();
                    newMessageRef.child(uuid).child("text").setValue((messageEditText.getText().toString()));
                    newMessageRef.child(uuid).child("name").setValue(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                    newMessageRef.child(uuid).child("uid").setValue(usrId);
                    newMessageRef.child(uuid).child("date").setValue(df.format(c.getTime()));

                    messageEditText.setText("");
                }
            }
        });

        adapter = new TopicAdapter(this,msgList);
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
        Intent i;
        i = new Intent(this, ProposalDetailsActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return i;
    }
}
