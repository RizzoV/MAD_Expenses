package it.polito.mad.team19.mad_expenses;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
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
import it.polito.mad.team19.mad_expenses.Classes.Expense;
import it.polito.mad.team19.mad_expenses.Classes.ExpenseDetail;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;

public class ExpenseDetailsActivity extends AppCompatActivity {

    private TextView expense_name;
    private TextView expense_desc;
    private TextView expense_cost;
    private TextView expense_author;
    private ImageView expense_img;
    private LinearLayout expense_details_listview;
    private String expenseAuthor;
    private String imgUrl;
    private String name;
    private String desc;
    private String cost;
    private String authorId;
    private String groupId;
    private String expenseId;
    static final int MODIFY_CODE = 17;
    private ArrayList<FirebaseGroupMember> contributorsList = new ArrayList<>();
    private ArrayList<FirebaseGroupMember> excludedList = new ArrayList<>();
    private ImageButton setPhotoButton;

    private AlertDialog alertDialog = null;

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
        expense_details_listview = (LinearLayout) findViewById(R.id.debtors_and_debts_listview);
        setPhotoButton = (ImageButton) findViewById((R.id.add_image_btn));

        expense_name.setText(name);
        expense_desc.setText(desc);
        expense_cost.setText(cost);
        expense_author.setText("loading...");

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

        final FirebaseDatabase fbDatabase = FirebaseDatabase.getInstance();

        DatabaseReference expenseContributorsRef = fbDatabase.getReference("gruppi").child(groupId).child("expenses").child(expenseId);
        expenseContributorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot contributor) {
                for (DataSnapshot contributors : contributor.child("contributors").getChildren()) {
                    Log.d("Contributor", contributors.toString());
                    for (DataSnapshot debtor : contributors.child("riepilogo").getChildren()) {
                        expenseDetailsList.add(new ExpenseDetail(contributors.child("nome").getValue().toString(), debtor.child("nome").getValue().toString(), debtor.child("amount").getValue().toString(), null, null));
                        edAdapter.setListData(expenseDetailsList);
                        edAdapter.notifyDataSetChanged();
                    }
                    contributorsList.add(new FirebaseGroupMember(contributors.getValue().toString(), null, contributors.getKey()));
                }
                for (DataSnapshot currentExcluded : contributor.child("excluded").getChildren())
                    excludedList.add(new FirebaseGroupMember(currentExcluded.getValue().toString(), null, currentExcluded.getKey()));

                for (int i = 0; i < edAdapter.getCount(); i++)
                    expense_details_listview.addView(edAdapter.getView(i, null, expense_details_listview));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ExpenseDetailsActivity", "Could not retrieve the list of debts");
            }
        });

        setPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ExpenseDetailsActivity.this, GalleryOrCameraDialog.class);
                startActivity(i);
                startActivity(i);

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
                if (expenseAuthor.equals(FirebaseAuth.getInstance().getCurrentUser().getUid().toString()))
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
        final String groupId = getIntent().getStringExtra("groupId");
        final String expenseId = getIntent().getStringExtra("ExpenseId");

        switch (id) {
            case R.id.deleteExpense: {
                //Dialog con istruzioni normali SENZA fragment

                alertDialog = new AlertDialog.Builder(this)
                        .setMessage(R.string.confirmExpenseDeletion)
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
                                FirebaseDatabase.getInstance().getReference("gruppi").child(groupId).child("expenses").child(expenseId).removeValue();
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
            case R.id.modifyExpense: {
                Log.d("ModifyExpense", "starting modify activity");

                Intent changeExpenseIntent = new Intent(this, AddExpenseActivity.class);
                changeExpenseIntent.putExtra("ExpenseName", name);
                changeExpenseIntent.putExtra("ExpenseImgUrl", imgUrl);
                changeExpenseIntent.putExtra("ExpenseDesc", desc);
                changeExpenseIntent.putExtra("ExpenseCost", cost);
                changeExpenseIntent.putExtra("ExpenseAuthorId", expenseAuthor);
                changeExpenseIntent.putExtra("groupId", groupId);
                changeExpenseIntent.putExtra("ExpenseId", expenseId);
                changeExpenseIntent.putExtra("ModifyIntent", "1");
                if (contributorsList != null) {
                    Bundle b = new Bundle();
                    b.putParcelableArrayList("contributorsList", contributorsList);
                    changeExpenseIntent.putExtra("contributorsBundle", b);
                }
                if (excludedList != null) {
                    Bundle e = new Bundle();
                    e.putParcelableArrayList("excludedList", excludedList);
                    changeExpenseIntent.putExtra("excludedBundle", e);

                    startActivityForResult(changeExpenseIntent, MODIFY_CODE);

                }
            }

            default:
                Log.e("ExpenseDetailsActivity", "Not finding a corresponding case to the menu item selected (" + id + ")");
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MODIFY_CODE && resultCode == RESULT_OK) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (alertDialog != null)
            alertDialog.dismiss();
    }

}