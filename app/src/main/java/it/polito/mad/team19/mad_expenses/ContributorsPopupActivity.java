package it.polito.mad.team19.mad_expenses;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
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
    String uid;
    ListView contributors_lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contributors_popup);
        //prendi la lista dei membri del gruppo
        String groupId = getIntent().getExtras().getString("groupId");

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("gruppi").child(groupId).child("membri");

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        contributors_lv = (ListView) findViewById(R.id.contributors_lv);
        final ArrayList<FirebaseGroupMember> contributors = new ArrayList<FirebaseGroupMember>();
        final GroupMembersAdapter adapter = new GroupMembersAdapter(this,contributors);
        contributors_lv.setAdapter(adapter);


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int nMembers = 0;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if(!uid.equals(child.getKey()))
                    {
                        contributors.add(new FirebaseGroupMember(child.child("nome").getValue().toString(),null,child.getKey()));
                        nMembers++;
                    }
                }

                if(nMembers==0)
                    Log.e("Contributors","no other members in the group!");
                else
                {
                    Log.e("no",contributors.toString());
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.95), (int)(height*.9));
    }
}
