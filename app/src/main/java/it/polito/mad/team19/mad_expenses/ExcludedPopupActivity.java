package it.polito.mad.team19.mad_expenses;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Adapters.GroupMembersAdapter;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;
import it.polito.mad.team19.mad_expenses.Classes.NetworkChangeReceiver;

public class ExcludedPopupActivity extends Activity {

    private FirebaseAuth mAuth;
    private String uid;
    private ListView contributors_lv;

    ArrayList<FirebaseGroupMember> selectedMembers = new ArrayList<>();

    NetworkChangeReceiver netChange;
    IntentFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contributors_popup);

        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netChange = new NetworkChangeReceiver();
        netChange.setViewForSnackbar(findViewById(android.R.id.content));
        netChange.setDialogShowTrue(false);
        registerReceiver(netChange, filter);

        TextView title = (TextView) findViewById(R.id.select_contributors_description_tv);
        title.setText(R.string.select_excluded_description);

        String groupId = getIntent().getExtras().getString("groupId");

        if (!getIntent().getBundleExtra("excludedBundle").getParcelableArrayList("excludedList").isEmpty())
        {
            selectedMembers = getIntent().getBundleExtra("excludedBundle").getParcelableArrayList("excludedList");
        }

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference groupMembersRef = database.getReference("gruppi").child(groupId).child("membri");

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        contributors_lv = (ListView) findViewById(R.id.contributors_lv);
        final ArrayList<FirebaseGroupMember> contributors = new ArrayList<FirebaseGroupMember>();
        final GroupMembersAdapter groupMembersAdapter = new GroupMembersAdapter(this, contributors, true);
        contributors_lv.setAdapter(groupMembersAdapter);

        contributors_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("DebugContributorsCheck", "Selected item no. " + position);
                FirebaseGroupMember selectedMember = (FirebaseGroupMember) parent.getItemAtPosition(position);
                if (!selectedMember.isChecked()) {
                    selectedMember.check(true);
                    selectedMembers.add(selectedMember);
                } else {
                    selectedMember.check(false);
                    for(FirebaseGroupMember fbgm : selectedMembers)
                        if(fbgm.getUid().equals(selectedMember.getUid())) {
                            selectedMembers.remove(fbgm);
                            break;
                        }
                }
                groupMembersAdapter.notifyDataSetChanged();
            }
        });

        FloatingActionButton contributorsFab = (FloatingActionButton) findViewById(R.id.select_contributors_fab);
        contributorsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle b = new Bundle();
                b.putParcelableArrayList("parceledExcluded", selectedMembers);
                intent.putExtras(b);
                setResult(RESULT_OK, intent);
                finish();
            }
        });


        groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int nMembers = 0;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String img = null;

                    if(child.child("immagine").exists())
                        img = child.child("immagine").getValue().toString();

                    contributors.add(new FirebaseGroupMember(child.child("nome").getValue().toString(), img, child.getKey()));
                    nMembers++;
                }
                if (nMembers == 0)
                    Log.d("Excluded", "no other members in the group!");
                else {
                    groupMembersAdapter.notifyDataSetChanged();
                }
                for (FirebaseGroupMember fbgm : selectedMembers) {
                    // Select them in the view

                    int itemPos = groupMembersAdapter.getPositionFromUid(fbgm.getUid());

                    if (itemPos != -1) {
                        //LUDO: per ceccare
                        contributors.get(itemPos).check(true);
                        groupMembersAdapter.notifyDataSetChanged();
                    } else
                        Log.e("ContributorsPopup", "Could not find the item corresponding to the UID" + fbgm.getUid() + "in the ListView");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ExcludedPopup", "Could not read group members");
            }
        });

        // Non occupare tutto lo schermo
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .95), (int) (height * .9));

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
}
