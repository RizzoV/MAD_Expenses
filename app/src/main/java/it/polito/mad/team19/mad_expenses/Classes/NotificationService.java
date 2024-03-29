package it.polito.mad.team19.mad_expenses.Classes;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOError;
import java.util.HashMap;

import it.polito.mad.team19.mad_expenses.ExpenseDetailsActivity;
import it.polito.mad.team19.mad_expenses.GroupActivity;
import it.polito.mad.team19.mad_expenses.GroupInfoActivity;
import it.polito.mad.team19.mad_expenses.GroupsListActivity;
import it.polito.mad.team19.mad_expenses.ProposalDetailsActivity;
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

    static {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

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
                if(dataSnapshot.hasChildren()) {
                    for (DataSnapshot group : dataSnapshot.getChildren()) {
                        final DatabaseReference notification = FirebaseDatabase.getInstance().getReference().child("notifications");
                        final String groupId = group.getKey();
                        if (group.child("nome").getValue() != null) {
                            final String groupName = group.child("nome").getValue().toString();

                            final String groupImage;
                            if(group.child("immagine").getValue()!=null)

                                groupImage = group.child("immagine").getValue().toString();
                            else
                                groupImage = null;

                            Log.d("Service", groupId);

                            notification.child(groupId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    DatabaseReference myNotRef = FirebaseDatabase.getInstance().getReference().child("utenti").child(uid).child("gruppi").child(groupId).child("notifiche");
                                    if (listenerNot.get(groupId) == null) {
                                        listenerNot.put(groupId, (int) dataSnapshot.getChildrenCount());

                                        //cancel displayed not
                                        myNotRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.getValue() != null)
                                                    if (displayedNot.size() > 0)
                                                        mNotificationManager.cancelAll();
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }

                                    if (listenerNot.get(groupId) < dataSnapshot.getChildrenCount()) {
                                        Log.d("Service", "new notification added");

                                        myNotRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()) {
                                                    try {
                                                        final String myNot = dataSnapshot.getValue().toString();
                                                        Log.d("Service", "mynot " + myNot);

                                                        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference().child("notifications").child(groupId);

                                                        ValueEventListener getNewNot = new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if (dataSnapshot.getChildrenCount() > 0) {
                                                                    for (DataSnapshot not : dataSnapshot.getChildren()) {
                                                                        if (!not.getKey().equals(myNot)) {
                                                                            Log.d("Service", "new not added to node");
                                                                            if (displayedNot.get(not.getKey()) == null) {
                                                                                Log.d("Service", "displayed not: " + not.toString());
                                                                                displayedNot.put(not.getKey(), 1);

                                                                                String uname = not.child("uname").getValue().toString();
                                                                                String text = "ND";

                                                                                if (not.child("activity").getValue().toString().equals(mContext.getResources().getString(R.string.notififcationAddExpenseActivity)))
                                                                                    text = uname + " " + mContext.getResources().getString(R.string.notififcationAddExpenseText);

                                                                                if (not.child("activity").getValue().toString().equals(mContext.getResources().getString(R.string.notififcationAddExpenseFromProposalActivity)))
                                                                                    text = uname + " " + mContext.getResources().getString(R.string.notififcationAddExpenseFromProposalText);

                                                                                if (not.child("activity").getValue().toString().equals(mContext.getResources().getString(R.string.notififcationAddProposalActivity)))
                                                                                    text = uname + " " + mContext.getResources().getString(R.string.notififcationAddProposalText);

                                                                                if (not.child("activity").getValue().toString().equals(mContext.getResources().getString(R.string.notififcationAddGroupActivity)))
                                                                                    text = uname + " " + mContext.getResources().getString(R.string.notififcationAddGroupText);

                                                                                if (not.child("activity").getValue().toString().equals(mContext.getResources().getString(R.string.notififcationAddMembersToGroupActivity)))
                                                                                    text = uname + " " + mContext.getResources().getString(R.string.notififcationAddMembersToGroupText);

                                                                                if (not.child("activity").getValue().toString().equals(mContext.getResources().getString(R.string.notififcationRemoveMembersToGroupActivity)))
                                                                                    text = uname + " " + mContext.getResources().getString(R.string.notififcationRemoveMembersToGroupText);

                                                                                if (not.child("activity").getValue().toString().equals(mContext.getResources().getString(R.string.notififcationAcceptedProposalActivity)))
                                                                                    text = uname + " " + mContext.getResources().getString(R.string.notififcationAcceptedProposalText);

                                                                                if (not.child("activity").getValue().toString().equals(mContext.getResources().getString(R.string.notififcationDenyProposalActivity)))
                                                                                    text = uname + " " + mContext.getResources().getString(R.string.notififcationDenyProposalText);

                                                                                if (not.child("activity").getValue().toString().equals(mContext.getResources().getString(R.string.notififcationDenyPayedDebtActivity)))
                                                                                    text = uname + " " + mContext.getResources().getString(R.string.notififcationDenyPayedDebtText);

                                                                                if (not.child("activity").getValue().toString().equals(mContext.getResources().getString(R.string.notififcationChangedExpenseBalancectivity)))
                                                                                    text = uname + " " + mContext.getResources().getString(R.string.notififcationChangedExpenseBalanceText);

                                                                                if (not.child("activity").getValue().toString().equals(mContext.getResources().getString(R.string.notififcationModifiedExpenseActivity)))
                                                                                    text = uname + " " + mContext.getResources().getString(R.string.notififcationModifiedExpenseText);

                                                                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
                                                                                mBuilder.setVibrate(new long[]{500, 500});
                                                                                mBuilder.setLights(Color.GREEN, 2000, 2000);
                                                                                //Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                                                Uri alarmSound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notitification);
                                                                                mBuilder.setSound(alarmSound);
                                                                                mBuilder.setSmallIcon(R.drawable.ic_notification_money);
                                                                                mBuilder.setContentTitle(groupName);
                                                                                mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round));
                                                                                mBuilder.setContentText(text);
                                                                                mBuilder.setAutoCancel(true);

                                                                                mBuilder.setPriority(Notification.PRIORITY_HIGH);
                                                                                TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
                                                                                stackBuilder.addParentStack(GroupsListActivity.class);
                                                                                Intent intent = new Intent(mContext, GroupsListActivity.class);

                                                                                String activity = not.child("activity").getValue().toString();

                                                                                if (activity.equals(getResources().getString(R.string.notififcationAddGroupActivity)))
                                                                                    intent = new Intent(mContext, GroupsListActivity.class);

                                                                                if (activity.equals(getResources().getString(R.string.notififcationAddMembersToGroupActivity))) {
                                                                                    intent = new Intent(mContext, GroupInfoActivity.class);
                                                                                    intent.putExtra("groupImage", groupImage);
                                                                                    intent.putExtra("groupName", groupName);
                                                                                    intent.putExtra("groupId", groupId);
                                                                                }

                                                                                if (activity.equals(getResources().getString(R.string.notififcationDenyPayedDebtActivity))) {
                                                                                    intent = new Intent(mContext, GroupActivity.class);
                                                                                    intent.putExtra("groupImage", groupImage);
                                                                                    intent.putExtra("groupName", groupName);
                                                                                    intent.putExtra("groupId", groupId);
                                                                                }

                                                                                if (activity.equals(getResources().getString(R.string.notififcationRemoveMembersToGroupActivity))) {
                                                                                    intent = new Intent(mContext, GroupInfoActivity.class);
                                                                                    intent.putExtra("groupImage", groupImage);
                                                                                    intent.putExtra("groupName", groupName);
                                                                                    intent.putExtra("groupId", groupId);
                                                                                }

                                                                                if (activity.equals(getResources().getString(R.string.notififcationAddExpenseActivity))) {
                                                                                    intent = new Intent(mContext, ExpenseDetailsActivity.class);
                                                                                    intent.putExtra("ExpenseName", not.child("ExpenseName").getValue().toString());
                                                                                    intent.putExtra("ExpenseDesc", not.child("ExpenseDesc").getValue().toString());
                                                                                    intent.putExtra("ExpenseAuthorId", not.child("ExpenseAuthorId").getValue().toString());
                                                                                    if (not.child("ExpenseImgUrl").getValue() != null)
                                                                                        intent.putExtra("ExpenseImgUrl", not.child("ExpenseImgUrl").getValue().toString());
                                                                                    if (not.child("ExpenseDate").getValue() != null)
                                                                                        intent.putExtra("ExpenseDate", not.child("ExpenseDate").getValue().toString());
                                                                                    intent.putExtra("ExpenseCost", not.child("ExpenseCost").getValue().toString());
                                                                                    intent.putExtra("ExpenseId", not.child("id").getValue().toString());
                                                                                    intent.putExtra("groupId", groupId);
                                                                                }

                                                                                if (activity.equals(getResources().getString(R.string.notififcationModifiedExpenseActivity))) {
                                                                                    intent = new Intent(mContext, ExpenseDetailsActivity.class);
                                                                                    intent.putExtra("ExpenseName", not.child("ExpenseName").getValue().toString());
                                                                                    intent.putExtra("ExpenseDesc", not.child("ExpenseDesc").getValue().toString());
                                                                                    intent.putExtra("ExpenseAuthorId", not.child("ExpenseAuthorId").getValue().toString());
                                                                                    if (not.child("ExpenseImgUrl").getValue() != null)
                                                                                        intent.putExtra("ExpenseImgUrl", not.child("ExpenseImgUrl").getValue().toString());
                                                                                    if (not.child("ExpenseDate").getValue() != null)
                                                                                        intent.putExtra("ExpenseDate", not.child("ExpenseDate").getValue().toString());
                                                                                    intent.putExtra("ExpenseCost", not.child("ExpenseCost").getValue().toString());
                                                                                    intent.putExtra("ExpenseId", not.child("id").getValue().toString());
                                                                                    intent.putExtra("groupId", groupId);
                                                                                }

                                                                                if (activity.equals(getResources().getString(R.string.notififcationChangedExpenseBalancectivity))) {
                                                                                    intent = new Intent(mContext, ExpenseDetailsActivity.class);
                                                                                    intent.putExtra("ExpenseName", not.child("ExpenseName").getValue().toString());
                                                                                    intent.putExtra("ExpenseDesc", not.child("ExpenseDesc").getValue().toString());
                                                                                    intent.putExtra("ExpenseAuthorId", not.child("ExpenseAuthorId").getValue().toString());
                                                                                    if (not.child("ExpenseImgUrl").getValue() != null)
                                                                                        intent.putExtra("ExpenseImgUrl", not.child("ExpenseImgUrl").getValue().toString());
                                                                                    if (not.child("ExpenseDate").getValue() != null)
                                                                                        intent.putExtra("ExpenseDate", not.child("ExpenseDate").getValue().toString());
                                                                                    intent.putExtra("ExpenseCost", not.child("ExpenseCost").getValue().toString());
                                                                                    intent.putExtra("ExpenseId", not.child("id").getValue().toString());
                                                                                    intent.putExtra("groupId", groupId);
                                                                                }

                                                                                if (activity.equals(getResources().getString(R.string.notififcationAddExpenseFromProposalActivity))) {
                                                                                    intent = new Intent(mContext, ExpenseDetailsActivity.class);
                                                                                    intent.putExtra("ExpenseName", not.child("ExpenseName").getValue().toString());
                                                                                    intent.putExtra("ExpenseDesc", not.child("ExpenseDesc").getValue().toString());
                                                                                    intent.putExtra("ExpenseAuthorId", not.child("ExpenseAuthorId").getValue().toString());
                                                                                    if (not.child("ExpenseImgUrl").getValue() != null)
                                                                                        intent.putExtra("ExpenseImgUrl", not.child("ExpenseImgUrl").getValue().toString());
                                                                                    if (not.child("ExpenseDate").getValue() != null)
                                                                                        intent.putExtra("ExpenseDate", not.child("ExpenseDate").getValue().toString());
                                                                                    intent.putExtra("ExpenseCost", not.child("ExpenseCost").getValue().toString());
                                                                                    intent.putExtra("ExpenseId", not.child("id").getValue().toString());
                                                                                    intent.putExtra("groupId", groupId);
                                                                                }

                                                                                if (activity.equals(getResources().getString(R.string.notififcationAcceptedProposalActivity))) {
                                                                                    intent = new Intent(mContext, ProposalDetailsActivity.class);
                                                                                    intent.putExtra("ProposalName", not.child("ProposalName").getValue().toString());
                                                                                    intent.putExtra("ProposalCost", not.child("ProposalCost").getValue().toString());
                                                                                    intent.putExtra("ProposalDesc", not.child("ProposalDesc").getValue().toString());
                                                                                    intent.putExtra("ProposalId", not.child("id").getValue().toString());
                                                                                    intent.putExtra("ProposalAuthorId", not.child("uid").getValue().toString());
                                                                                    intent.putExtra("groupId", groupId);
                                                                                }

                                                                                if (activity.equals(getResources().getString(R.string.notififcationDenyProposalActivity))) {
                                                                                    intent = new Intent(mContext, ProposalDetailsActivity.class);
                                                                                    intent.putExtra("ProposalName", not.child("ProposalName").getValue().toString());
                                                                                    intent.putExtra("ProposalCost", not.child("ProposalCost").getValue().toString());
                                                                                    intent.putExtra("ProposalDesc", not.child("ProposalDesc").getValue().toString());
                                                                                    intent.putExtra("ProposalId", not.child("id").getValue().toString());
                                                                                    intent.putExtra("ProposalAuthorId", not.child("uid").getValue().toString());
                                                                                    intent.putExtra("groupId", groupId);
                                                                                }

                                                                                if (activity.equals(getResources().getString(R.string.notififcationAddProposalActivity))) {
                                                                                    intent = new Intent(mContext, ProposalDetailsActivity.class);
                                                                                    intent.putExtra("ProposalName", not.child("ProposalName").getValue().toString());
                                                                                    intent.putExtra("ProposalCost", not.child("ProposalCost").getValue().toString());
                                                                                    intent.putExtra("ProposalDesc", not.child("ProposalDesc").getValue().toString());
                                                                                    intent.putExtra("ProposalId", not.child("id").getValue().toString());
                                                                                    intent.putExtra("ProposalAuthorId", not.child("uid").getValue().toString());
                                                                                    intent.putExtra("groupId", groupId);
                                                                                }


                                                                                stackBuilder.addNextIntent(intent);
                                                                                PendingIntent resultPendingIntent = PendingIntent.getActivities(mContext, 0,
                                                                                        new Intent[]{new Intent(mContext, GroupsListActivity.class), intent}, PendingIntent.FLAG_UPDATE_CURRENT);

                                                                                mBuilder.setContentIntent(resultPendingIntent);
                                                                                mBuilder.setOnlyAlertOnce(true);


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
                                                    } catch (IOError e) {
                                                        Log.d("MyNotErr", e.toString());
                                                    }
                                                }
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
