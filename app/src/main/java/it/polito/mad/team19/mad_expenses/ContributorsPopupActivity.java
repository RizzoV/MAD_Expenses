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
import java.util.concurrent.TimeUnit;

import it.polito.mad.team19.mad_expenses.Adapters.GroupMembersAdapter;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;

/**
 * Created by Valentino on 11/04/2017.
 */

public class ContributorsPopupActivity extends Activity {

    private FirebaseAuth mAuth;
    private String uid;
    private ListView contributors_lv;

    final ArrayList<FirebaseGroupMember> contributors = new ArrayList<>();
    final GroupMembersAdapter groupMembersAdapter = new GroupMembersAdapter(this, contributors);

    ArrayList<FirebaseGroupMember> selectedMembers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contributors_popup);
        // Prendi la lista dei membri del gruppo
        String groupId = getIntent().getExtras().getString("groupId");

        // Non occupare tutto lo schermo
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .95), (int) (height * .9));

        // Check if there already are some selected members
        if (!getIntent().getBundleExtra("contributorsBundle").getParcelableArrayList("contributorsList").isEmpty()) {
            selectedMembers = getIntent().getBundleExtra("contributorsBundle").getParcelableArrayList("contributorsList");
        }

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference groupMembersRef = database.getReference("gruppi").child(groupId).child("membri");

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        contributors_lv = (ListView) findViewById(R.id.contributors_lv);
        contributors_lv.setAdapter(groupMembersAdapter);

        groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int nMembers = 0;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    contributors.add(new FirebaseGroupMember(child.child("nome").getValue().toString(), null, child.getKey()));
                    nMembers++;
                }
                if (nMembers == 0)
                    Log.d("Contributors", "no other members in the group!");
                else {
                    groupMembersAdapter.notifyDataSetChanged();
                }

                contributors_lv.invalidate();
                //TODO: far funzionare sta listview, con le selezioni prese precedentemente
                // Preselect eventual already selected members
                for (FirebaseGroupMember fbgm : selectedMembers) {
                    // Select them in the view
                    int itemPos = groupMembersAdapter.getPositionFromUid(fbgm.getUid());
                    Log.e("Già checkato: ", fbgm.getName() + ", posizione: " + String.valueOf(itemPos));
                    if (itemPos != -1) {
                        CheckBox check = (CheckBox) groupMembersAdapter.getView(itemPos, null, contributors_lv).findViewById(R.id.contributor_checkbox);
                        check.setChecked(true);
                        check.invalidate();
                        Log.e("DEBUG", "Status of check n." + itemPos + ": " + check.isChecked());
                    } else
                        Log.e("ContributorsPopup", "Could not find the item corresponding to the UID" + fbgm.getUid() + "in the ListView");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ContributorsPopup", "Could not read group members");
            }
        });

        contributors_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("DebugContributorsCheck", "Selected item no. " + position);
                CheckBox contributorCheckBox = (CheckBox) view.findViewById(R.id.contributor_checkbox);
                FirebaseGroupMember selectedMember = (FirebaseGroupMember) parent.getItemAtPosition(position);
                Log.e("DEBUG", "Now you clicked - Was " + contributorCheckBox.isChecked());
                if (!contributorCheckBox.isChecked()) {
                    contributorCheckBox.setChecked(true);
                    Boolean found = Boolean.FALSE;
                    for (FirebaseGroupMember fbgm : selectedMembers) {
                        if (fbgm.getUid().equals(selectedMember.getUid())) {
                            found = Boolean.TRUE;
                            break;
                        }
                    }
                    if (!found)
                        selectedMembers.add(selectedMember);
                } else {
                    contributorCheckBox.setChecked(false);
                    for (int i = 0; i < selectedMembers.size(); i++) {
                        if (selectedMembers.get(i).getUid().equals(selectedMember.getUid()))
                            selectedMembers.remove(i);
                    }
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
    }

}
