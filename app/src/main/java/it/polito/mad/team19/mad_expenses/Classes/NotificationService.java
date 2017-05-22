package it.polito.mad.team19.mad_expenses.Classes;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.icu.text.LocaleDisplayNames;
import android.icu.text.StringPrepParseException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import it.polito.mad.team19.mad_expenses.R;


/**
 * Created by ikkoyeah on 22/05/17.
 */

public class NotificationService extends IntentService{

    HashMap<String,Integer> listenerNot = new HashMap<>();
    HashMap<String,Integer> displayedNot = new HashMap<>();
    Context mContext;
    String uid;
    NotificationManager  mNotificationManager;
    protected FirebaseAuth.AuthStateListener mAuthStateListener;
    protected FirebaseAuth mAuth;


    public NotificationService()
    {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d("Service","Started");

    }

    private void getGroupsList(final Context mContext)
    {
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("utenti").child(uid).child("gruppi");
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChildren())
                {
                    for(DataSnapshot group : dataSnapshot.getChildren())
                    {
                        final DatabaseReference notification = FirebaseDatabase.getInstance().getReference().child("notifications");
                        final String groupId = group.getKey();
                        final String groupName = group.child("nome").getValue().toString();
                        Log.d("Service",groupId);

                        notification.child(groupId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                DatabaseReference myNotRef = FirebaseDatabase.getInstance().getReference().child("utenti").child(uid).child("gruppi").child(groupId).child("notifiche");
                                if(listenerNot.get(groupId)==null)
                                {
                                    listenerNot.put(groupId, (int) dataSnapshot.getChildrenCount());

                                    //cancel displayed not
                                    myNotRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.getValue()!=null)
                                                if(displayedNot.size()>0)
                                                    mNotificationManager.cancelAll();

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }

                                if(listenerNot.get(groupId)<dataSnapshot.getChildrenCount())
                                {
                                    Log.d("Service","new notification added");

                                    myNotRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot)
                                        {
                                            final String myNot = dataSnapshot.getValue().toString();
                                            Log.d("Service","mynot "+myNot);

                                            DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference().child("notifications").child(groupId);

                                            ValueEventListener getNewNot = new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.getChildrenCount()>1)
                                                    {
                                                        for(DataSnapshot not :  dataSnapshot.getChildren())
                                                        {
                                                            if(!not.getKey().equals(myNot))
                                                            {
                                                                Log.d("Service","new not added to node");
                                                                if(displayedNot.get(not.getKey())==null)
                                                                {
                                                                    Log.d("Service","displayed not: "+not.toString());
                                                                    displayedNot.put(not.getKey(), 1);

                                                                    String uname = not.child("uname").getValue().toString();
                                                                    String text = "ND";

                                                                    if(not.child("activity").getValue().toString().equals(mContext.getResources().getString(R.string.notififcationAddExpenseActivity)))
                                                                        text = uname + " " + mContext.getResources().getString(R.string.notififcationAddExpenseText);

                                                                    if(not.child("activity").getValue().toString().equals(mContext.getResources().getString(R.string.notififcationAddGroupActivity)))
                                                                        text = uname + " " + mContext.getResources().getString(R.string.notififcationAddGroupText);

                                                                    if(not.child("activity").getValue().toString().equals(mContext.getResources().getString(R.string.notififcationAddMembersToGroupActivity)))
                                                                        text = uname + " " + mContext.getResources().getString(R.string.notififcationAddMembersToGroupText);

                                                                    if(not.child("activity").getValue().toString().equals(mContext.getResources().getString(R.string.notififcationRemoveMembersToGroupActivity)))
                                                                        text = uname + " " + mContext.getResources().getString(R.string.notififcationRemoveMembersToGroupText);



                                                                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
                                                                    mBuilder.setSmallIcon(R.drawable.ic_not_piggy);
                                                                    mBuilder.setContentTitle(groupName);
                                                                    mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round));
                                                                    mBuilder.setContentText(text);

                                                                    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                                                    mNotificationManager.notify(displayedNot.size(), mBuilder.build());
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            };

                                            notificationRef.orderByKey().startAt(myNot).addListenerForSingleValueEvent(getNewNot);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mContext = this;
        FirebaseApp.initializeApp(getApplicationContext());


        mAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && !user.isAnonymous()) {
                    uid = user.getUid();
                    getGroupsList(mContext);
                }
            }
        };

        mAuth.addAuthStateListener(mAuthStateListener);
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Service","destroyed");

        Intent i = new Intent(mContext,NotificationService.class);
        sendBroadcast(i);
    }


}
