package it.polito.mad.team19.mad_expenses;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import it.polito.mad.team19.mad_expenses.Adapters.ExpenseDetailsAdapter;
import it.polito.mad.team19.mad_expenses.Classes.ExpenseDetail;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;
import it.polito.mad.team19.mad_expenses.Classes.NetworkChangeReceiver;

public class ExpenseDetailsActivity extends AppCompatActivity {

    private static final int MODIFIED = 8;
    private TextView expense_name;
    private TextView expense_desc;
    private TextView expense_cost;
    private TextView expense_author;
    private ImageView expense_img;
    private LinearLayout expense_details_listview;
    private String expenseAuthor;
    //private String currentPersonalBalance;
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
    private ImageButton set_photo_button;
    private TextView set_photo_text_view;

    private AlertDialog alertDialog = null;

    NetworkChangeReceiver netChange;
    IntentFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_details);
        setTitle("Dettagli Spesa");
        getSupportActionBar().setHomeButtonEnabled(true);

        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netChange = new NetworkChangeReceiver();
        netChange.setViewForSnackbar(findViewById(android.R.id.content));
        netChange.setDialogShowTrue(false);
        registerReceiver(netChange, filter);

        name = getIntent().getStringExtra("ExpenseName");
        desc = getIntent().getStringExtra("ExpenseDesc");
        imgUrl = getIntent().getStringExtra("ExpenseImgUrl");
        authorId = getIntent().getStringExtra("ExpenseAuthorId");
        cost = getIntent().getStringExtra("ExpenseCost");
        groupId = getIntent().getStringExtra("groupId");
        expenseId = getIntent().getStringExtra("ExpenseId");
        //currentPersonalBalance = getIntent().getStringExtra("currentPersonalBalance");

        expense_name = (TextView) findViewById(R.id.expense_name);
        expense_desc = (TextView) findViewById(R.id.expense_description);
        expense_cost = (TextView) findViewById(R.id.expense_cost);
        expense_img = (ImageView) findViewById(R.id.expense_photo);
        expense_author = (TextView) findViewById(R.id.expense_author_value);
        expense_details_listview = (LinearLayout) findViewById(R.id.debtors_and_debts_listview);
        set_photo_button = (ImageButton) findViewById(R.id.add_image_btn);
        set_photo_text_view = (TextView) findViewById(R.id.add_expense_photo_tv);

        expense_name.setText(name);
        expense_desc.setText(desc);
        //TODO: gestire currency diverse dall'Euro
        expense_cost.setText(cost + " " + Currency.getInstance("EUR").getSymbol());
        expense_author.setText("loading...");

        final FirebaseDatabase firebase = FirebaseDatabase.getInstance();

        DatabaseReference dbAuthorNameRef = firebase.getReference("gruppi").child(groupId).child("membri").child(authorId).child("nome").getRef();
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

        DatabaseReference expenseRef = firebase.getReference("gruppi").child(groupId).child("expenses").child(expenseId);
        expenseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot expense) {
                for (DataSnapshot contributor : expense.child("contributors").getChildren()) {
                    String contributor_img = null;
                    if (contributor.child("immagine").exists())
                        contributor_img = contributor.child("immagine").getValue().toString();

                    for (DataSnapshot debtor : contributor.child("riepilogo").getChildren()) {

                        String debtor_img = null;
                        if (debtor.child("immagine").exists())
                            debtor_img = debtor.child("immagine").getValue().toString();

                        expenseDetailsList.add(new ExpenseDetail(contributor.child("nome").getValue().toString(), debtor.child("nome").getValue().toString(), contributor.getKey(), debtor.getKey(), String.format(Locale.getDefault(), "%.2f", Float.valueOf(debtor.child("amount").getValue(String.class))), contributor_img, debtor_img));
                        edAdapter.notifyDataSetChanged();
                    }
                    contributorsList.add(new FirebaseGroupMember(contributor.child("nome").getValue(String.class), contributor.child("immagine").getValue(String.class), contributor.getKey()));
                }
                for (DataSnapshot currentExcluded : expense.child("excluded").getChildren()) {
                    excludedList.add(new FirebaseGroupMember(currentExcluded.child("nome").getValue(String.class), currentExcluded.child("immagine").getValue(String.class), currentExcluded.getKey()));
                    Log.e("EDA E in", currentExcluded.child("nome").getValue(String.class) + "-");
                }

                // Vale: dialog per la modifica dell'importo dovuto
                for (int i = 0; i < edAdapter.getCount(); i++) {
                    final View itemView = edAdapter.getView(i, null, expense_details_listview);
                    final String expenseContributorId = ((ExpenseDetail) edAdapter.getItem(i)).getCreditorId();
                    final String expenseDebtorId = ((ExpenseDetail) edAdapter.getItem(i)).getDebtorId();
                    final int position = i;
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(expenseContributorId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                View dialogView = getLayoutInflater().inflate(R.layout.dialogboxlayout_edit_debit, null);
                                final EditText debtEditText = (EditText) dialogView.findViewById(R.id.debt_edit_text);
                                debtEditText.setText(((ExpenseDetail) edAdapter.getItem(position)).getAmount());

                                AlertDialog alertDialog = new AlertDialog.Builder(ExpenseDetailsActivity.this)
                                        .setView(dialogView)
                                        .setTitle(R.string.modify_debt_value)
                                        .setPositiveButton(getString(R.string.edit), null)
                                        .setNegativeButton(getString(R.string.cancel), null)
                                        .create();

                                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(final DialogInterface dialog) {
                                        Button buttonPositive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                        buttonPositive.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (debtEditText.getText().toString().trim().isEmpty()) {
                                                    debtEditText.setError(getString(R.string.mandatory_field));
                                                } else if (!debtEditText.getText().toString().trim().matches(AddExpenseActivity.COST_REGEX)) {
                                                    debtEditText.setError(getString(R.string.invalid_cost_field));
                                                } else {
                                                    DatabaseReference debtAmountRef = firebase.getReference().child("gruppi").child(groupId).child("expenses").child(expenseId).
                                                            child("debtors").child(expenseDebtorId).child("riepilogo").child(expenseContributorId).child("amount");
                                                    DatabaseReference creditAmountRef = firebase.getReference().child("gruppi").child(groupId).child("expenses").child(expenseId).
                                                            child("contributors").child(expenseContributorId).child("riepilogo").child(expenseDebtorId).child("amount");
                                                    /* DatabaseReference creditAmount = firebase.getReference().child("utenti").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                     *      .child("gruppi").child(groupId).child("credito");
                                                     * DatabaseReference debtAmount = firebase.getReference().child("utenti").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                     *       .child("gruppi").child(groupId).child("debito");
                                                     */

                                                    String chosenAmount = debtEditText.getText().toString().trim().replace(",", ".");
                                                    debtAmountRef.setValue("-" + chosenAmount);
                                                    creditAmountRef.setValue(chosenAmount);

                                                    ((ExpenseDetail) edAdapter.getItem(position)).setAmount(String.format(Locale.getDefault(), "%.2f", Float.valueOf(chosenAmount)));
                                                    edAdapter.notifyDataSetChanged();

                                                    ((TextView) itemView.findViewById(R.id.debt_amount)).setText(
                                                         String.format(Locale.getDefault(), "%.2f", Float.valueOf(chosenAmount)) + " " + Currency.getInstance(Locale.ITALY).getSymbol());

                                                    if (Float.valueOf(chosenAmount) > 0)
                                                      itemView.findViewById(R.id.debt_amount).setBackground(ContextCompat.getDrawable(ExpenseDetailsActivity.this, R.drawable.rounded_corners_red));
                                                    else
                                                      itemView.findViewById(R.id.debt_amount).setBackground(ContextCompat.getDrawable(ExpenseDetailsActivity.this, R.drawable.rounded_corners_green));

                                                    dialog.dismiss();

                                                }
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
                                // Apertura automatica della tastiera
                                alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                                alertDialog.show();

                            } else {
                                Snackbar.make(findViewById(android.R.id.content), R.string.cannot_modify_non_creditor, Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });

                    expense_details_listview.addView(itemView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ExpenseDetailsActivity", "Could not retrieve the list of debts");
            }
        });

        DatabaseReference expenseImageRef = expenseRef.child("image");
        expenseImageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String imgUrl = dataSnapshot.getValue(String.class);
                if (imgUrl != null) {
                    set_photo_text_view.setText(R.string.loading_image);
                    Log.d("ExpenseDetailsActivity", "Debug image url:" + imgUrl);
                    showExpenseImage(imgUrl);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ExpenseDetailActivity", "Could not retrieve the expense's image");
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
                if (expenseAuthor.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
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
                Bundle b = new Bundle();
                if (contributorsList != null) {
                    b.putParcelableArrayList("contributorsList", contributorsList);
                }
                if (excludedList != null) {
                    b.putParcelableArrayList("excludedList", excludedList);
                }

                changeExpenseIntent.putExtras(b);
                startActivityForResult(changeExpenseIntent, MODIFY_CODE);
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
            setResult(MODIFIED, getIntent());
            Bundle b = new Bundle();
            b.putParcelableArrayList("contributors", data.getParcelableArrayListExtra("contributors"));
            b.putParcelableArrayList("excluded", data.getParcelableArrayListExtra("excluded"));
            getIntent().putExtras(b);
            getIntent().putExtra("expenseId", data.getStringExtra("expenseId"));
            getIntent().putExtra("expenseTotal", data.getStringExtra("expenseTotal"));
            getIntent().putExtra("expenseUId", data.getStringExtra("expenseUId"));
            getIntent().putExtra("expenseUserName", data.getStringExtra("expenseUserName"));
            finish();
        }
    }


    private void showExpenseImage(String imageUrl) {
        try {
            Glide.with(this).load(imageUrl).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().error(R.drawable.circle).into(new BitmapImageViewTarget(expense_img) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    expense_img.setImageDrawable(circularBitmapDrawable);

                    set_photo_text_view.setVisibility(View.GONE);
                }
            });
        } catch (Exception e) {
            Log.e("ExpenseDetailsActivity", "Exception:\n" + e.toString());
        }
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

        if (alertDialog != null)
            alertDialog.dismiss();

        if (netChange != null) {
            netChange.closeSnack();
            unregisterReceiver(netChange);
            netChange = null;
            Log.d("Receiver", "unregister on pause");
        }

    }
}