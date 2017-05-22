package it.polito.mad.team19.mad_expenses;

import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;

/**
 * Created by Valentino on 22/05/2017.
 */

public class AsyncProposalWaitingForLoader extends AsyncTask<Void, Void, Void> {

    private String proposalId;
    private String groupId;
    private DatabaseReference databaseReference;

    public AsyncProposalWaitingForLoader(DatabaseReference databaseReference, String proposalId, String groupId) {
        this.proposalId = proposalId;
        this.groupId = groupId;
        this.databaseReference = databaseReference;
    }

    @Override
    protected Void doInBackground(Void... params) {

        final ArrayList<FirebaseGroupMember> groupMembers = new ArrayList<>();

        DatabaseReference groupMembersReference  = databaseReference.child("gruppi").child(groupId).child("membri");
        groupMembersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()) {
                    for(DataSnapshot member : dataSnapshot.getChildren()) {
                        groupMembers.add(new FirebaseGroupMember(member.child("nome").getValue(String.class), member.child("immagine").getValue(String.class), member.getKey()));
                    }

                    for(FirebaseGroupMember fbgm : groupMembers) {
                        databaseReference.child("gruppi").child(groupId).child("proposals").child(proposalId).child("waitingFor").child(fbgm.getUid()).setValue(fbgm.getName());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AsyncProposalWaitingFor", "Could not retrieve the list of group members");
            }
        });

        return null;
    }
}
