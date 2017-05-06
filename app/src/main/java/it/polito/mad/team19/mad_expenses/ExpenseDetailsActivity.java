package it.polito.mad.team19.mad_expenses;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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

    private TextView expense_name;
    private TextView expense_desc;
    private TextView expense_cost;
    private TextView expense_author;
    private ImageView expense_img;
    private ListView expense_details_listview;
    private String expenseAuthor;
    private String imgUrl;
    private String name;
    private String desc;
    private String cost;
    private String authorId;
    private String groupId;
    private String expenseId;
    static final int MODIFY_CODE = 17;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_details);
        setTitle("Dettagli Spesa");
        getSupportActionBar().setHomeButtonEnabled(true);

        name = getIntent().getStringExtra("ExpenseName");
        desc = getIntent().getStringExtra("ExpenseDesc");
        imgUrl = getIntent().getStringExtra("ExpenseImgUrl");
        authorId = getIntent().getStringExtra("ExpenseAuthorId");
        cost = getIntent().getStringExtra("ExpenseCost");
        groupId = getIntent().getStringExtra("groupId");
        expenseId = getIntent().getStringExtra("ExpenseId");


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

        // TODO: monitorare tutti i contribuenti iniziali e non solo l'autore della spesa
        DatabaseReference dbAuthorNameRef = FirebaseDatabase.getInstance().getReference("gruppi").child(groupId).child("membri").child(authorId).child("nome").getRef();
        dbAuthorNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                expenseAuthor = dataSnapshot.getValue().toString();
                expense_author.setText(expenseAuthor);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ExpenseDetailsActivity", "Expense author username reading cancelled");
            }
        });

        final ArrayList<ExpenseDetail> expenseDetailsList = new ArrayList<>();
        final ExpenseDetailsAdapter edAdapter = new ExpenseDetailsAdapter(this, expenseDetailsList);
        expense_details_listview.setAdapter(edAdapter);

        final FirebaseDatabase fbDatabase = FirebaseDatabase.getInstance();

        DatabaseReference expenseContributorsRef = fbDatabase.getReference("gruppi").child(groupId).child("expenses").child(expenseId).child("contributors");
        expenseContributorsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                final DatabaseReference groupMembersDbRef = fbDatabase.getReference("utenti").child(dataSnapshot.getKey()).child("bilancio").child(groupId);
                groupMembersDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot2) {
                        for (DataSnapshot child : dataSnapshot2.getChildren()) {
                            if(child.child("riepilogo").child(expenseId).exists()) {
                                expenseDetailsList.add(new ExpenseDetail(dataSnapshot.getValue().toString(), child.child("nome").getValue().toString(), child.child("riepilogo").child(expenseId).getValue().toString(), null, null));
                                edAdapter.setListData(expenseDetailsList);
                                edAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("ExpenseDetailsActivity","Unable to read group members");
                    }
                });
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
    public boolean onCreateOptionsMenu(Menu menu) {
        final Menu finalMenu = menu;
        String groupId = getIntent().getStringExtra("groupId");
        String expenseId = getIntent().getStringExtra("ExpenseId");

        DatabaseReference authorRef = FirebaseDatabase.getInstance().getReference("gruppi").child(groupId).child("expenses").child(expenseId).child("author");
        authorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String expenseAuthor = dataSnapshot.getValue(String.class);
                if(expenseAuthor.equals(FirebaseAuth.getInstance().getCurrentUser().getUid().toString()))
                    // Inflate the menu; this adds items to the action bar if it is present.
                    getMenuInflater().inflate(R.menu.menu_expense_details, finalMenu);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ExpenseDetailsActivity", "Unable to read expense author");
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        String groupId = getIntent().getStringExtra("groupId");
        String expenseId = getIntent().getStringExtra("ExpenseId");

        switch (id) {
            case R.id.deleteExpense: {
                DialogFragment newFragment = new ConfirmExpenseDeletionDialogFragment(this, groupId, expenseId);
                newFragment.show(getSupportFragmentManager(), "confirmExpenseDeletion");

                return true;
            }
            case R.id.modifyExpense: {
                Log.d("ModifyExpense","starting modify activity");

                Intent changeExpenseIntent = new Intent(this, AddExpenseActivity.class);
                changeExpenseIntent.putExtra("ExpenseName", name);
                changeExpenseIntent.putExtra("ExpenseImgUrl", imgUrl);
                changeExpenseIntent.putExtra("ExpenseDesc", desc);
                changeExpenseIntent.putExtra("ExpenseCost", cost);
                changeExpenseIntent.putExtra("ExpenseAuthorId", expenseAuthor);
                changeExpenseIntent.putExtra("groupId", groupId);
                changeExpenseIntent.putExtra("ExpenseId", expenseId);
                changeExpenseIntent.putExtra("ModifyIntent", "1");
                startActivityForResult(changeExpenseIntent, MODIFY_CODE);
            }

            default:
                Log.e("ExpenseDetailsActivity", "Not finding a corresponding case to the menu item selected (" + id + ")");
                return super.onOptionsItemSelected(item);
        }
    }

    public static class ConfirmExpenseDeletionDialogFragment extends DialogFragment {

        private String groupId;
        private String expenseId;
        private Context context;

        public ConfirmExpenseDeletionDialogFragment(Context context, String groupId, String expenseId) {
            this.context = context;
            this.groupId = groupId;
            this.expenseId = expenseId;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.confirmExpenseDeletion)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            FirebaseDatabase.getInstance().getReference("gruppi").child(groupId).child("expenses").child(expenseId).removeValue();
                            ((Activity) context).finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Nothing happens
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

}





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



/*        // Get group members
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
        });*/




