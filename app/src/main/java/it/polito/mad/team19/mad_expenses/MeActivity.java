package it.polito.mad.team19.mad_expenses;

import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import it.polito.mad.team19.mad_expenses.Adapters.MeRecyclerAdapter;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;
import it.polito.mad.team19.mad_expenses.Classes.Me;
import it.polito.mad.team19.mad_expenses.Classes.NetworkChangeReceiver;

public class MeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String groupId;
    TextView credito_tv;
    TextView debito_tv;
    ArrayList<Me> otherMembersList = new ArrayList<>();
    ArrayList<FirebaseGroupMember> groupMembersList = new ArrayList<>();

    ImageView my_thumb;

    NetworkChangeReceiver netChange;
    IntentFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);
        getSupportActionBar().setTitle(getResources().getString(R.string.personal_balance));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netChange = new NetworkChangeReceiver();
        netChange.setViewForSnackbar(findViewById(android.R.id.content));
        netChange.setDialogShowTrue(false);
        registerReceiver(netChange, filter);

        groupId = getIntent().getStringExtra("groupId");

        my_thumb = (ImageView) findViewById(R.id.me_activity_thumb);

        credito_tv = (TextView) findViewById(R.id.credito_tv);
        debito_tv = (TextView) findViewById(R.id.debito_tv);

        final ScrollView sw = (ScrollView) findViewById(R.id.scrollView);

        sw.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                sw.post(new Runnable() {
                    public void run() {
                        sw.fullScroll(View.FOCUS_UP);
                    }
                });
            }
        });

        TextView me_username_tv = (TextView) findViewById(R.id.me_username_tv);

        //Ludo: informazioni utente da fb

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser().getPhotoUrl() != null) {
            Glide.with(this).load(mAuth.getCurrentUser().getPhotoUrl()).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_user_noimg).centerCrop().error(R.drawable.ic_user_noimg).into(new BitmapImageViewTarget(my_thumb) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(getResources(), resource);

                    circularBitmapDrawable.setCircular(true);
                    my_thumb.setImageDrawable(circularBitmapDrawable);
                }
            });
        } else
            my_thumb.setImageDrawable(getResources().getDrawable(R.drawable.ic_user_noimg));
        String uname = mAuth.getCurrentUser().getDisplayName();
        if (uname == null)
            uname = "User";
        else if (uname.trim().isEmpty())
            uname = "User";

        me_username_tv.setText(uname);

        getMembers();
    }

    private void getMembers() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("gruppi").child(groupId).child("membri");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.d("MembriSnap", dataSnapshot.getValue().toString());
                    if (child.child("immagine").hasChildren())
                        groupMembersList.add(new FirebaseGroupMember(child.child("nome").getValue().toString(), child.child("immagine").getValue().toString(), child.getKey()));
                    else
                        groupMembersList.add(new FirebaseGroupMember(child.child("nome").getValue().toString(), null, child.getKey()));
                }

                getBalance();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MeActivity", "Unable to get the group memebers");
            }
        });
    }

    private void getBalance() {

        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.fromto_rv);
        final MeRecyclerAdapter adapter = new MeRecyclerAdapter(this, otherMembersList);
        mRecyclerView.setAdapter(adapter);

        LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
        mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManagerVertical);

        float debito = 0;
        float credito = 0;

        /* VALE
         * Prendi le informazioni dal Bundle passato tramite l'intent
         */

        ArrayList<Me> balancesArray = getIntent().getBundleExtra("balancesBundle").getParcelableArrayList("balancesArray");

        if (balancesArray == null) {
            Log.e("MeActivity", "balancesArray è NULL");
            return;
        }

        for (Me otherMember : balancesArray) {
            otherMembersList.add(otherMember);
            if (otherMember.getAmount() > 0)
                credito += otherMember.getAmount();
            else
                debito += otherMember.getAmount();
        }

        adapter.notifyDataSetChanged();


        /* Vale
         * Azzeramento credito
         */
        adapter.setOnItemClickListener(new MeRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final View rowView, int position) {

                final Me balance = adapter.getItemAtPosition(position);
                final String otherId = balance.getId();

                if(balance.getAmount() > 0 ) {
                    AlertDialog alertDialog = new AlertDialog.Builder(MeActivity.this)
                            .setTitle(R.string.confirmDebtExtinctionTitle)
                            .setMessage(R.string.confirmDebtExtinction)
                            .setPositiveButton(getString(R.string.yes), null)
                            .setNegativeButton(getString(R.string.no), null)
                            .create();

                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(final DialogInterface dialog) {
                            Button buttonPositive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                            final String userId = mAuth.getCurrentUser().getUid();
                            buttonPositive.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    final DatabaseReference expensesRef = FirebaseDatabase.getInstance().getReference().child("gruppi").child(groupId)
                                            .child("expenses");
                                    expensesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot expense : dataSnapshot.getChildren()) {
                                                if (expense.child("contributors").child(userId)
                                                        .child("riepilogo").child(otherId).exists()) {
                                                    expense.child("contributors").child(userId).child("riepilogo").child(otherId).child("amount").getRef().setValue("0");
                                                    expense.child("debtors").child(otherId).child("riepilogo").child(userId).child("amount").getRef().setValue("0");
                                                } else if (expense.child("contributors").child(otherId)
                                                        .child("riepilogo").child(userId).exists()) {
                                                    expense.child("contributors").child(otherId).child("riepilogo").child(userId).child("amount").getRef().setValue("0");
                                                    expense.child("debtors").child(userId).child("riepilogo").child(otherId).child("amount").getRef().setValue("0");
                                                }
                                            }

                                            /* Vale
                                             * Per visualizzare subito il valore aggiornato senza aspettare Firebase
                                             */
                                            subtractCredit(balance.getAmount());
                                            balance.setAmount((float) 0);
                                            adapter.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.e("MeActivity", "Unable to extinguish the debt");
                                        }
                                    });
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
                    alertDialog.show();
                }
                else if(balance.getAmount() < 0 ){
                    Snackbar.make(findViewById(android.R.id.content), R.string.cannotExtinguish, Snackbar.LENGTH_LONG).show();
                }
            }
        });


        for (int i = 0; i < otherMembersList.size(); i++)
            Log.d("meBalance", otherMembersList.get(i).getName() + " " + otherMembersList.get(i).getAmount().toString());

        //Ludo: grafico a torta
        PieChart pieChart = (PieChart) findViewById(R.id.chart);

        List<PieEntry> entries = new ArrayList<>();

        if (debito != 0)
            entries.add(new PieEntry(-debito, "Debito"));
        if (credito != 0)
            entries.add(new PieEntry(credito, "Credito"));

        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

        pieChart.setDescription(null);

        PieDataSet set = new PieDataSet(entries, "Debito/Credito");

        if (debito != 0 && credito != 0)
            set.setColors(new int[]{ R.color.redMaterial, R.color.textGreen }, getApplicationContext());
        else {
            if (debito != 0)
                set.setColors(new int[]{R.color.redMaterial}, getApplicationContext());
            if (credito != 0)
                set.setColors(new int[]{R.color.textGreen}, getApplicationContext());
        }

        set.setValueTextSize(18);
        set.setValueTextColor(Color.WHITE);
        PieData data = new PieData(set);
        pieChart.setData(data);
        pieChart.invalidate(); // refresh

        if (debito < 0)
            debito = -debito;
        debito_tv.setText(String.format(Locale.getDefault(), "%.2f", debito) + " " + Currency.getInstance(Locale.ITALY).getSymbol());

        credito_tv.setText(String.format(Locale.getDefault(), "%.2f", credito) + " " + Currency.getInstance(Locale.ITALY).getSymbol());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
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

        if (netChange != null) {
            netChange.closeSnack();
            unregisterReceiver(netChange);
            netChange = null;
            Log.d("Receiver", "unregister on pause");
        }

    }

    public void subtractCredit(Float amount) {
        String fields[] = credito_tv.getText().toString().split(" ");
        credito_tv.setText(String.format(Locale.getDefault(), "%.2f", Float.valueOf(fields[0].replace(",", ".")) - amount) + " " + Currency.getInstance(Locale.ITALY).getSymbol());
    }
}
