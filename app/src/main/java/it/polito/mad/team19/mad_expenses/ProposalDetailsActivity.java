package it.polito.mad.team19.mad_expenses;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProposalDetailsActivity extends AppCompatActivity {

    private String desc;
    private String name;
    private String cost;
    private TextView author_tv;
    private TextView cost_tv;
    private TextView desc_tv;
    private TextView name_tv;
    private CardView cw_topic;
    private Button accept;
    private Button deny;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private String proposalId;
    private String groupId;
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proposal_details);

        getSupportActionBar().setTitle("Dettagli Proposta");

        cost = getIntent().getStringExtra("ProposalCost");
        name = getIntent().getStringExtra("ProposalName");
        desc = getIntent().getStringExtra("ProposalDesc");
        proposalId = getIntent().getStringExtra("proposalId");
        groupId = getIntent().getStringExtra("groupId");


        final String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        final String userImgUrl = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();

        cw_topic = (CardView) findViewById(R.id.proposal_topic_cw);
        author_tv = (TextView) findViewById(R.id.proposal_author_value);
        cost_tv = (TextView) findViewById(R.id.proposal_cost);
        desc_tv = (TextView) findViewById(R.id.proposal_description);
        name_tv = (TextView) findViewById(R.id.proposal_name);
        accept = (Button) findViewById(R.id.btn_accept_proposal);
        deny = (Button) findViewById(R.id.btn_deny_proposal);

        desc_tv.setText(desc);
        cost_tv.setText(cost);
        name_tv.setText(name);

        cw_topic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProposalDetailsActivity.this, TopicActivity.class);
                i.putExtra("topicType","proposals");
                i.putExtra("topicName",name);
                i.putExtra("groupId", groupId);
                i.putExtra("proposalId", proposalId);
                startActivity(i);
            }
        });

        // Set listeners on voting buttons
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(ProposalDetailsActivity.this)
                        .setTitle(R.string.confirmProposalVoteAccept)
                        .setPositiveButton(getString(R.string.yes), null)
                        .setNegativeButton(getString(R.string.no), null)
                        .create();

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button buttonPositive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        buttonPositive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // If the user is in the list of accepters, remove him from there
                                database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("refusers").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChildren()) {
                                            for(DataSnapshot refuser : dataSnapshot.getChildren()) {
                                                if(refuser.getKey().equals(userId)) {
                                                    refuser.getRef().removeValue();
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e("ProposalDetailsActivity", "Could not retrieve the list of accepters 2");
                                    }
                                });

                                // Add the user to the list of accepters
                                database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("accepters").child(userId).child("nome").setValue(username);
                                database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("accepters").child(userId).child("immagine").setValue(userImgUrl);
                            }
                        });

                        Button buttonNegative = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        buttonNegative.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.cancel();
                            }
                        });
                    }
                });
                alertDialog.show();

            }
        });

        deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(ProposalDetailsActivity.this)
                        .setTitle(R.string.confirmProposalVoteDeny)
                        .setPositiveButton(getString(R.string.yes), null)
                        .setNegativeButton(getString(R.string.no), null)
                        .create();

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button buttonPositive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        buttonPositive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // If the user is in the list of accepters, remove him from there
                                database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("accepters").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChildren()) {
                                            for(DataSnapshot accepter : dataSnapshot.getChildren()) {
                                                if(accepter.getKey().equals(userId)) {
                                                    accepter.getRef().removeValue();
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e("ProposalDetailsActivity", "Could not retrieve the list of accepters 2");
                                    }
                                });

                                // Add the user to the list of refusers
                                database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("refusers").child(userId).child("nome").setValue(username);
                                database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("refusers").child(userId).child("immagine").setValue(userImgUrl);
                            }
                        });

                        Button buttonNegative = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        buttonNegative.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.cancel();
                            }
                        });
                    }
                });
                alertDialog.show();
            }
        });

        // Add listeners on the lists of accepters and refusers of the proposal
        final TextView acceptersTextView = (TextView) findViewById(R.id.accepted_list_string);
        DatabaseReference acceptersRef = database.getReference().child("groups").child(groupId).child("proposals").child(proposalId).child("accepters");
        acceptersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String acceptersList = new String();
                if(dataSnapshot.hasChildren()) {
                    for (DataSnapshot accepter : dataSnapshot.getChildren()) {

                        // If the user is a accepter, grey out the refuse accept
                        if(accepter.getKey().equals(userId)) {
                            accept.setAlpha(.5f);
                            accept.setClickable(false);
                            deny.setAlpha(1f);
                            deny.setClickable(true);
                        }

                        // Add the accepter to the list of accepters
                        if(acceptersList.length() == 0)
                            acceptersList.concat(accepter.child("name").getValue(String.class));
                        else
                            acceptersList.concat(", " + accepter.child("name").getValue(String.class));
                    }
                }
                if(acceptersList.length() != 0)
                    acceptersTextView.setText(acceptersList);
                else
                    acceptersTextView.setText(getResources().getString(R.string.nobody));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ProposalDetailsActivity", "Could not retrieve the list of accepters");
            }
        });

        final TextView refusersTextView = (TextView) findViewById(R.id.refused_list_string);
        DatabaseReference refusersRef = database.getReference().child("groups").child(groupId).child("proposals").child(proposalId).child("refusers");
        acceptersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String refusersList = new String();
                if(dataSnapshot.hasChildren()) {
                    for (DataSnapshot refuser : dataSnapshot.getChildren()) {

                        // If the user is a refuser, grey out the refuse button
                        if(refuser.getKey().equals(userId)) {
                            deny.setAlpha(.5f);
                            deny.setClickable(false);
                            accept.setAlpha(1f);
                            accept.setClickable(true);
                        }

                        // Add the refuser to the list of refusers
                        if(refusersList.length() == 0)
                            refusersList.concat(refuser.child("name").getValue(String.class));
                        else
                            refusersList.concat(", " + refuser.child("name").getValue(String.class));
                    }
                }
                if(refusersList.length() != 0)
                    refusersTextView.setText(refusersList);
                else
                    refusersTextView.setText(getResources().getString(R.string.nobody));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ProposalDetailsActivity", "Could not retrieve the list of refusers");
            }
        });
    }
}
