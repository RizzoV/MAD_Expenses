package it.polito.mad.team19.mad_expenses;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Adapters.GroupMembersAdapter;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;

/**
 * Created by Valentino on 11/04/2017.
 */

public class ContributorsPopupActivity extends Activity {

    private FirebaseAuth mAuth;
    private String uid;
    private ListView contributors_lv;

    ArrayList<FirebaseGroupMember> selectedMembers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contributors_popup);
        // Prendi la lista dei membri del gruppo
        String groupId = getIntent().getExtras().getString("groupId");

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference groupMembersRef = database.getReference("gruppi").child(groupId).child("membri");

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        contributors_lv = (ListView) findViewById(R.id.contributors_lv);
        final ArrayList<FirebaseGroupMember> contributors = new ArrayList<FirebaseGroupMember>();
        final GroupMembersAdapter groupMembersAdapter = new GroupMembersAdapter(this, contributors);
        contributors_lv.setAdapter(groupMembersAdapter);

        contributors_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("DebugContributorsCheck", "Selected item no. " + position);
                CheckBox contributorCheckBox = (CheckBox) view.findViewById(R.id.contributor_checkbox);
                if (!contributorCheckBox.isChecked()) {
                    contributorCheckBox.setChecked(true);
                    selectedMembers.add((FirebaseGroupMember) parent.getItemAtPosition(position));
                } else {
                    contributorCheckBox.setChecked(false);
                    selectedMembers.remove(parent.getItemAtPosition(position));
                }
                contributorCheckBox.invalidate();
            }
        });

        FloatingActionButton contributorsFab = (FloatingActionButton) findViewById(R.id.select_contributors_fab);
        contributorsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle b = new Bundle();
                b.putParcelableArrayList("parceledContributors", selectedMembers);
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
                    if (!uid.equals(child.getKey())) {
                        contributors.add(new FirebaseGroupMember(child.child("nome").getValue().toString(), null, child.getKey()));
                        nMembers++;
                    }
                }
                if (nMembers == 0)
                    Log.d("Contributors", "no other members in the group!");
                else {
                    groupMembersAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ContributorsPopupActivi", "Could not read group members");
            }
        });

        // Non occupare tutto lo schermo
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .95), (int) (height * .9));

    }
}
