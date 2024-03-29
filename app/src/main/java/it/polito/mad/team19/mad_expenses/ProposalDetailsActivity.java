package it.polito.mad.team19.mad_expenses;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;
import it.polito.mad.team19.mad_expenses.NotActivities.AsyncCurrencyConverter;
import uk.co.senab.photoview.PhotoView;

public class ProposalDetailsActivity extends AppCompatActivity {

    private Button accept;
    private Button deny;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private String proposalId;
    private String groupId;
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private ImageView proposal_img;
    private TextView set_photo_tv;
    private String imgUrl;

    private AlertDialog alertDialog = null;
    private AlertDialog alertDialog1 = null;

    private Double exchangeRate = 1d;

    private Dialog fullscreenImageDialog;

    @Override
    protected void onPause() {
        super.onPause();

        if(alertDialog != null)
            if(alertDialog.isShowing())
                alertDialog.dismiss();

        if(alertDialog2 != null)
            if(alertDialog2.isShowing())
                alertDialog.dismiss();

        if(alertDialog1 != null)
            if(alertDialog1.isShowing())
                alertDialog.dismiss();

        if (fullscreenImageDialog != null)
            if (fullscreenImageDialog.isShowing())
                fullscreenImageDialog.dismiss();
    }

    private AlertDialog alertDialog2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proposal_details);

        getSupportActionBar().setTitle("Dettagli Proposta");

        final String desc;
        final String name;
        final String cost;
        String authorId;


        final TextView author_tv;
        TextView cost_tv;
        TextView desc_tv;
        TextView name_tv;
        CardView cw_topic;
        cw_topic = (CardView) findViewById(R.id.proposal_topic_cw);
        author_tv = (TextView) findViewById(R.id.proposal_author_value);
        cost_tv = (TextView) findViewById(R.id.proposal_cost);
        desc_tv = (TextView) findViewById(R.id.proposal_description);
        name_tv = (TextView) findViewById(R.id.proposal_name);
        accept = (Button) findViewById(R.id.btn_accept_proposal);
        deny = (Button) findViewById(R.id.btn_deny_proposal);
        proposal_img = (ImageView) findViewById(R.id.proposal_photo);
        set_photo_tv = (TextView) findViewById(R.id.add_proposals_photo_tv);

        cost = getIntent().getStringExtra("ProposalCost");
        name = getIntent().getStringExtra("ProposalName");
        desc = getIntent().getStringExtra("ProposalDesc");
        proposalId = getIntent().getStringExtra("ProposalId");
        groupId = getIntent().getStringExtra("groupId");

        // Retrieve the author name
        authorId = getIntent().getStringExtra("ProposalAuthorId");
        database.getReference().child("gruppi").child(groupId).child("membri").child(authorId).child("nome").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                author_tv.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ProposalDetailsActivity", "Could not retrieve the name of he author");
            }
        });

        final String userImgUrl;

        final String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()!=null)
            userImgUrl = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
        else
            userImgUrl = "none";

        desc_tv.setText(desc);
        name_tv.setText(name);


        imgUrl = getIntent().getStringExtra("ProposalImgUrl");
        if (imgUrl != null) {
            set_photo_tv.setText(R.string.loading_image);
            showExpenseImage(imgUrl);
        }


        database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot proposal) {

                final String[] userCurrencyCode = new String[1];

                userCurrencyCode[0] = getSharedPreferences("currencySetting", MODE_PRIVATE).getString("currency", Currency.getInstance(Locale.getDefault()).getCurrencyCode());

                if(!"EUR".equals(userCurrencyCode[0])) {
                    try {
                        exchangeRate = new AsyncCurrencyConverter(ProposalDetailsActivity.this, userCurrencyCode[0]).execute().get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                // Per evitare crash
                if(exchangeRate == null)
                    exchangeRate = 1d;
                cost_tv.setText(String.format(Locale.getDefault(), "%.2f", Float.valueOf(cost.replace(",", ".")) * exchangeRate) + " " + Currency.getInstance(userCurrencyCode[0]).getSymbol());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ProposalDetailsActivity", "Could not read the proposal");
            }
        });

        cw_topic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProposalDetailsActivity.this, TopicActivity.class);
                i.putExtra("topicType", "proposals");
                i.putExtra("topicName", name);
                i.putExtra("groupId", groupId);
                i.putExtra("proposalId", proposalId);
                startActivity(i);
            }
        });

        // Set listeners on voting buttons
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog1 = new AlertDialog.Builder(ProposalDetailsActivity.this)
                        .setTitle(R.string.confirmProposalVoteAccept)
                        .setPositiveButton(getString(R.string.yes), null)
                        .setNegativeButton(getString(R.string.no), null)
                        .create();

                alertDialog1.setOnShowListener(new DialogInterface.OnShowListener() {
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
                                        if (dataSnapshot.hasChildren()) {
                                            for (DataSnapshot refuser : dataSnapshot.getChildren()) {
                                                if (refuser.getKey().equals(userId)) {
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

                                // If the user is in the waitingFor list, remove him from here
                                database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("waitingFor").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChildren()) {
                                            for (DataSnapshot user : dataSnapshot.getChildren()) {
                                                if (user.getKey().equals(userId)) {
                                                    user.getRef().removeValue();
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e("ProposalDetailsActivity", "Could not retrieve the waitingFor list 2");
                                    }
                                });

                                // Add the user to the list of accepters
                                database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("accepters").child(userId).child("nome").setValue(username);
                                database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("accepters").child(userId).child("immagine").setValue(userImgUrl);

                                final DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference().child("notifications").child(groupId);
                                final String notificationId = notificationRef.push().getKey();

                                String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

                                if (username == null)
                                    username = "User";

                                Calendar c = Calendar.getInstance();
                                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy-HH-mm", Locale.getDefault());
                                final String formattedDate = df.format(c.getTime());

                                HashMap<String, Object> notification = new HashMap<>();

                                notification.put("activity", getString(R.string.notififcationAcceptedProposalActivity));

                                notification.put("data", formattedDate);
                                notification.put("id", proposalId);
                                notification.put("uid", userId);
                                notification.put("uname", username);
                                notification.put("groupId", groupId);
                                notification.put("ProposalName", name);
                                notification.put("ProposalDesc", desc);
                                notification.put("ProposalCost", cost);

                                notificationRef.child(notificationId).updateChildren(notification);

                                DatabaseReference myNotRef = FirebaseDatabase.getInstance().getReference().child("utenti").child(userId).child("gruppi").child(groupId).child("notifiche");
                                myNotRef.setValue(notificationId);


                                dialog.dismiss();
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
                alertDialog1.show();

            }
        });

        deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog2 = new AlertDialog.Builder(ProposalDetailsActivity.this)
                        .setTitle(R.string.confirmProposalVoteDeny)
                        .setPositiveButton(getString(R.string.yes), null)
                        .setNegativeButton(getString(R.string.no), null)
                        .create();

                alertDialog2.setOnShowListener(new DialogInterface.OnShowListener() {
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
                                        if (dataSnapshot.hasChildren()) {
                                            for (DataSnapshot accepter : dataSnapshot.getChildren()) {
                                                if (accepter.getKey().equals(userId)) {
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

                                // If the user is in the waitingFor list, remove him from here
                                database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("waitingFor").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChildren()) {
                                            for (DataSnapshot user : dataSnapshot.getChildren()) {
                                                if (user.getKey().equals(userId)) {
                                                    user.getRef().removeValue();
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e("ProposalDetailsActivity", "Could not retrieve the waitingFor list 3");
                                    }
                                });

                                // Add the user to the list of refusers
                                database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("refusers").child(userId).child("nome").setValue(username);
                                database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("refusers").child(userId).child("immagine").setValue(userImgUrl);

                                final DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference().child("notifications").child(groupId);
                                final String notificationId = notificationRef.push().getKey();

                                String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

                                if (username == null)
                                    username = "User";

                                Calendar c = Calendar.getInstance();
                                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy-HH-mm", Locale.getDefault());
                                final String formattedDate = df.format(c.getTime());

                                HashMap<String, Object> notification = new HashMap<>();

                                notification.put("activity", getString(R.string.notififcationDenyProposalActivity));

                                notification.put("data", formattedDate);
                                notification.put("id", proposalId);
                                notification.put("uid", userId);
                                notification.put("uname", username);
                                notification.put("groupId", groupId);
                                notification.put("ProposalName", name);
                                notification.put("ProposalDesc", desc);
                                notification.put("ProposalCost", cost);
                                notificationRef.child(notificationId).updateChildren(notification);

                                DatabaseReference myNotRef = FirebaseDatabase.getInstance().getReference().child("utenti").child(userId).child("gruppi").child(groupId).child("notifiche");
                                myNotRef.setValue(notificationId);

                                dialog.dismiss();
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
                alertDialog2.show();
            }
        });

        // Add listeners on the lists of accepters and refusers of the proposal
        final TextView acceptersTextView = (TextView) findViewById(R.id.accepted_list_string);
        DatabaseReference acceptersRef = database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("accepters");
        acceptersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String acceptersList = new String();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot accepter : dataSnapshot.getChildren()) {

                        // If the user is an accepter, grey out the accept button
                        if (accepter.getKey().equals(userId)) {
                            accept.setAlpha(.3f);
                            accept.setClickable(false);
                            deny.setAlpha(1f);
                            deny.setClickable(true);
                        }

                        // Add the accepter to the list of accepters
                        if (acceptersList.length() == 0)
                            acceptersList = acceptersList.concat(accepter.child("nome").getValue(String.class));
                        else
                            acceptersList = acceptersList.concat(", " + accepter.child("nome").getValue(String.class));
                    }
                }
                if (acceptersList.length() != 0)
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
        DatabaseReference refusersRef = database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("refusers");
        refusersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String refusersList = new String();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot refuser : dataSnapshot.getChildren()) {

                        // If the user is a refuser, grey out the refuse button
                        if (refuser.getKey().equals(userId)) {
                            deny.setAlpha(.3f);
                            deny.setClickable(false);
                            accept.setAlpha(1f);
                            accept.setClickable(true);
                        }

                        // Add the refuser to the list of refusers
                        if (refusersList.length() == 0)
                            refusersList = refusersList.concat(refuser.child("nome").getValue(String.class));
                        else
                            refusersList = refusersList.concat(", " + refuser.child("nome").getValue(String.class));
                    }
                }
                if (refusersList.length() != 0)
                    refusersTextView.setText(refusersList);
                else
                    refusersTextView.setText(getResources().getString(R.string.nobody));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ProposalDetailsActivity", "Could not retrieve the list of refusers");
            }
        });

        // Add listeners on the lists of accepters and refusers of the proposal
        final TextView waitingForTextView = (TextView) findViewById(R.id.waiting_list_string);
        DatabaseReference waitingRef = database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("waitingFor");
        waitingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String waitingList = new String();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot user : dataSnapshot.getChildren()) {

                        // Add the user to waitingList
                        if (waitingList.length() == 0)
                            waitingList = waitingList.concat(user.getValue(String.class));
                        else
                            waitingList = waitingList.concat(", " + user.getValue(String.class));
                    }
                }
                if (waitingList.length() != 0)
                    waitingForTextView.setText(waitingList);
                else
                    waitingForTextView.setText(getResources().getString(R.string.nobody));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ProposalDetailsActivity", "Could not retrieve the waitingFor node");
            }
        });

        // Show the possibility to transform the proposal into an expense if everybody has voted
        final CardView transformPropInExpense_cv = (CardView) findViewById(R.id.card_transform_prop_in_expense);
        DatabaseReference waitingForRef = database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("waitingFor");
        waitingForRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren())
                    transformPropInExpense_cv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ProposalDetailsActivity", "could not retrieve the waitingFor list");
            }
        });

        transformPropInExpense_cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<FirebaseGroupMember> contributors = new ArrayList<>();

                String photourl = null;

                if(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()!=null)
                    photourl = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();

                contributors.add(new FirebaseGroupMember(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), photourl, userId, 0f));

                database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("refusers").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<FirebaseGroupMember> excluded = new ArrayList<>();

                        if (dataSnapshot.hasChildren()) {
                            for (DataSnapshot refuser : dataSnapshot.getChildren()) {
                                excluded.add(new FirebaseGroupMember(refuser.child("nome").getValue(String.class), refuser.child("immagine").getValue(String.class), refuser.getKey(), 0f));
                            }
                        }

                        // Make the expense creation activity start
                        Intent i = new Intent(ProposalDetailsActivity.this, AddExpenseActivity.class);
                        i.putExtra("ExpenseName", name);
                        i.putExtra("ExpenseDesc", desc);
                        i.putExtra("ExpenseImgUrl", imgUrl);
                        i.putExtra("ExpenseAuthorId", userId);
                        i.putExtra("ExpenseCost", String.valueOf(Double.parseDouble(cost) * exchangeRate));
                        i.putExtra("groupId", groupId);
                        i.putExtra("ExpenseId,", "fake");
                        i.putExtra("ModifyIntent", "true");
                        i.putExtra("CreateExpenseFromProposal", "true");
                        i.putExtra("butDoNotTrack", "true");
                        i.putExtra("contributorsList", contributors);
                        i.putExtra("excludedList", excluded);
                        i.putExtra("ProposalId", proposalId);
                        startActivity(i);

                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("ProposalDetailsActivity", "Could not retrieve the list of refusers 17");
                    }
                });
            }
        });


        // Click listener on the image
        if (imgUrl != null) {
            fullscreenImageDialog = new Dialog(ProposalDetailsActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            fullscreenImageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            fullscreenImageDialog.setCancelable(true);
            fullscreenImageDialog.setContentView(R.layout.expense_image_fullscreen);
            Button btnClose = (Button) fullscreenImageDialog.findViewById(R.id.btnIvClose);
            PhotoView ivPreview = (PhotoView) fullscreenImageDialog.findViewById(R.id.iv_fullscreen_image);

            Glide.with(this).load(imgUrl).asBitmap().error(R.drawable.circle).into(new BitmapImageViewTarget(ivPreview) {
                @Override
                protected void setResource(Bitmap resource) {
                    ivPreview.setImageBitmap(resource);
                    fullscreenImageDialog.findViewById(R.id.fullscreen_loading_tv).setVisibility(View.GONE);
                }
            });

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    fullscreenImageDialog.dismiss();
                }
            });

            proposal_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fullscreenImageDialog.show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String proposalAuthorId = getIntent().getStringExtra("ProposalAuthorId");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (proposalAuthorId.equals(mAuth.getCurrentUser().getUid()))
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_proposal_details, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        final String groupId = getIntent().getStringExtra("groupId");
        final String proposalId = getIntent().getStringExtra("ProposalId");

        switch (id) {
            case R.id.deleteProposal: {
                //Dialog con istruzioni normali SENZA fragment

                alertDialog = new AlertDialog.Builder(this)
                        .setMessage(R.string.confirm_proposal_deletion)
                        .setPositiveButton(getString(R.string.yes), null)
                        .setNegativeButton(getString(R.string.cancel), null)
                        .create();

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button buttonPositive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        buttonPositive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FirebaseDatabase.getInstance().getReference("gruppi").child(groupId).child("proposals").child(proposalId).removeValue();
                                finish();
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

                return true;
            }

            default:
                Log.e("ExpenseDetailsActivity", "Not finding a corresponding case to the menu item selected (" + id + ")");
                return super.onOptionsItemSelected(item);
        }
    }


    private void showExpenseImage(String imageUrl) {
        try {
            Glide.with(this).load(imageUrl).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().error(R.drawable.circle).into(new BitmapImageViewTarget(proposal_img) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    proposal_img.setImageDrawable(circularBitmapDrawable);

                    set_photo_tv.setVisibility(View.GONE);
                }
            });
        } catch (Exception e) {
            Log.e("ExpenseDetailsActivity", "Exception:\n" + e.toString());
        }

    }
}
