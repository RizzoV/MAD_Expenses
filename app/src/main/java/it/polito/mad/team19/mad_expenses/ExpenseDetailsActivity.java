package it.polito.mad.team19.mad_expenses;

import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Adapters.ExpenseDetailsAdapter;
import it.polito.mad.team19.mad_expenses.Classes.ExpenseDetail;

public class ExpenseDetailsActivity extends AppCompatActivity {

    TextView expense_name;
    TextView expense_desc;
    TextView expense_cost;
    TextView expense_author;
    ImageView expense_img;
    ListView expense_details_listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_details);
        setTitle("Dettagli Spesa");
        getSupportActionBar().setHomeButtonEnabled(true);

        String name = getIntent().getStringExtra("ExpenseName");
        String desc = getIntent().getStringExtra("ExpenseDesc");
        String imgUrl = getIntent().getStringExtra("ExpenseImgUrl");
        String cost = getIntent().getStringExtra("ExpenseCost");
        String authorId = getIntent().getStringExtra("ExpenseAuthorId");
        final String groupId = getIntent().getStringExtra("groupId");
        final String expenseId = getIntent().getStringExtra("ExpenseId");

        expense_name = (TextView) findViewById(R.id.expense_name);
        expense_desc = (TextView) findViewById(R.id.expense_description);
        expense_cost = (TextView) findViewById(R.id.expense_cost);
        expense_img = (ImageView) findViewById(R.id.expense_photo);
        expense_author = (TextView) findViewById(R.id.expense_author_value);
        expense_details_listview = (ListView) findViewById(R.id.debtors_and_debts_listview);

        expense_name.setText(name);
        expense_desc.setText(desc);
        expense_cost.setText(cost);
        expense_author.setText("loading...");

        DatabaseReference dbAuthorNameRef = FirebaseDatabase.getInstance().getReference("gruppi").child(groupId).child("membri").child(authorId).child("nome").getRef();
        dbAuthorNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                expense_author.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ExpenseDetailsActivity", "Expense author username reading cancelled");
            }
        });


        /*TODO: temporaneamente disattivato perchè faceva crashare
        //Jured: gestito il caso in cui arrivi un link che indica l'assenza di immagine
        if (imgUrl == null) {
            expense_img.setImageResource(R.drawable.circle);
        } else {
            Log.e("DebugExpenseDetails", imgUrl);

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReferenceFromUrl(imgUrl);

            final long ONE_MEGABYTE = 1024 * 1024;
            storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    expense_img.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }*/


        /*
         * Manage expense balance (debts and intersections)
         */

        // Get group members
        final ArrayList<ExpenseDetail> expenseDetailsList = new ArrayList<>();
        final ExpenseDetailsAdapter edAdapter = new ExpenseDetailsAdapter(this, expenseDetailsList);
        expense_details_listview.setAdapter(edAdapter);

        final FirebaseDatabase fbDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference groupMembersDbRef = fbDatabase.getReference("gruppi").child(groupId).child("membri").getRef();
        groupMembersDbRef.addChildEventListener(new ChildEventListener() {
            @Override
            // For each group member
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Get his username
                final String userName = dataSnapshot.child("nome").getValue().toString();
                Log.e("utente in analisi", dataSnapshot.getKey());
                // And set up a listener on his balance for this expense
                DatabaseReference childBalanceDbRef = FirebaseDatabase.getInstance().getReference("utenti").child(dataSnapshot.getKey()).child("bilancio").child(groupId).getRef();
                childBalanceDbRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.child("riepilogo").child(expenseId).exists()) {

                            // Then the currently analyzed user has an intersection on this expense with another user, now we should check if it is a credit or a debt
                            // We only process the case in which it is a debt, credits would be redundant
                            final float amount = Float.parseFloat(dataSnapshot.child("riepilogo").child(expenseId).getValue().toString());
                            if (amount < 0) {
                                Log.e("Test", String.valueOf(amount));
                                // Retrieve the creditor's name
                                DatabaseReference creditorNameReference = groupMembersDbRef.child(dataSnapshot.getKey()).child("nome").getRef();
                                creditorNameReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Log.e("Test", dataSnapshot.getValue().toString());
                                        // Add the debt to the expenseDetailsList
                                        expenseDetailsList.add(new ExpenseDetail(dataSnapshot.getValue().toString(), userName, String.valueOf(amount*(-1)), null, null));
                                        edAdapter.setListData(expenseDetailsList);
                                        edAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e("ExpenseDetailsActivity", "failed to grab creditor name");
                                    }
                                });


                            }
                        }

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //TODO: non può succedere?
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    //TODO: rimuovi
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // Do nothing
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ExpenseDetailsActivity", "Group members read failed");
            }
        });


    }

}
