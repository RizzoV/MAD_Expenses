package it.polito.mad.team19.mad_expenses;

import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Bitmap;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import it.polito.mad.team19.mad_expenses.Adapters.MeRecyclerAdapter;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;
import it.polito.mad.team19.mad_expenses.Classes.Me;
import it.polito.mad.team19.mad_expenses.Classes.NetworkChangeReceiver;
import it.polito.mad.team19.mad_expenses.NotActivities.AsyncCurrencyConverter;

public class MeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String groupId;
    TextView credito_tv;
    TextView debito_tv;
    ArrayList<Me> otherMembersList = new ArrayList<>();
    ArrayList<FirebaseGroupMember> groupMembersList = new ArrayList<>();

    LineChart chart;

    ImageView my_thumb;

    NetworkChangeReceiver netChange;
    IntentFilter filter;
    AlertDialog alertDialog;

    Spinner chartViewSpinner;

    String customCurrencyCode;
    Double exchangeRate = 1d;


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
        chartViewSpinner = (Spinner) findViewById(R.id.chartViewSpinner);

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


        // Vale: gestione valute
        customCurrencyCode = getSharedPreferences("currencySetting", MODE_PRIVATE).getString("currency", Currency.getInstance(Locale.getDefault()).getCurrencyCode());
        // Ottieni il tasso di scambio
        if (!customCurrencyCode.equals("EUR")) {
            try {
                exchangeRate = (new AsyncCurrencyConverter(this, customCurrencyCode)).execute().get();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("AddExpenseActivity", e.getMessage());
            }
        }

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

                getChart();
                getBalance();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MeActivity", "Unable to get the group memebers");
            }
        });
    }

    private void getChart()
    {
        //Ludo grafico storico

        chart = (LineChart) findViewById(R.id.chart);

        //Ludo: hashmap da popolare per i grafici

        HashMap<Integer,Float> daysCredit = new HashMap<>();
        HashMap<Integer,Float> monthsCredit = new HashMap<>();
        HashMap<Integer,Float> yearsCredit = new HashMap<>();
        HashMap<Integer,Float> daysDebit = new HashMap<>();
        HashMap<Integer,Float> monthsDebit = new HashMap<>();
        HashMap<Integer,Float> yearsDebit = new HashMap<>();

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate[] = df.format(c.getTime()).split("-");

        int currentDay = Integer.parseInt(formattedDate[0]);
        int currentMonth = Integer.parseInt(formattedDate[1]);
        int currentYear = Integer.parseInt(formattedDate[2]);


        FirebaseAuth mauth = FirebaseAuth.getInstance();
        String uid = mauth.getCurrentUser().getUid();

        DatabaseReference getBalance = FirebaseDatabase.getInstance().getReference().child("utenti").child(uid).child("gruppi").child(groupId).child("bilancio");
        getBalance.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Snap",dataSnapshot.toString());
                boolean first = true;
                int startingYear = 2017;
                for(DataSnapshot year : dataSnapshot.getChildren())
                {
                    float yearCredit = 0;
                    float yearDebit = 0;
                    if(first) {
                        startingYear = Integer.parseInt(year.getKey());
                    }

                    float monthCredit = 0;
                    float monthDebit =0;

                    for(DataSnapshot month : year.getChildren())
                    {

                        for(DataSnapshot day: month.getChildren())
                        {
                            float dayCredit = Float.parseFloat(day.child("credito").getValue().toString());
                            float dayDebit = Float.parseFloat(day.child("debito").getValue().toString());

                            daysCredit.put(Integer.parseInt(day.getKey()),dayCredit);
                            daysDebit.put(Integer.parseInt(day.getKey()),dayDebit);

                            if(monthCredit<dayCredit)
                                monthCredit+=dayCredit-monthCredit;
                            if(monthDebit<dayDebit)
                                monthDebit+=dayDebit-monthDebit;
                        }
                        monthsCredit.put(Integer.parseInt(month.getKey()),monthCredit);
                        monthsDebit.put(Integer.parseInt(month.getKey()),monthDebit);

                        if(yearCredit<monthCredit)
                            yearCredit+=monthCredit-yearCredit;
                        if(yearDebit<monthDebit)
                            yearDebit+=monthDebit-yearDebit;
                    }
                    yearsCredit.put(Integer.parseInt(year.getKey()),yearCredit);
                    yearsDebit.put(Integer.parseInt(year.getKey()),yearDebit);
                }

                //Ludo: il primo grafico che viene visulizzaro quando si pare l'acitivty
                setChartDayView(daysCredit,daysDebit,currentMonth,currentDay);

                int finalStartingYear = startingYear;
                chartViewSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        switch(position){
                            case 0:
                                setChartDayView(daysCredit,daysDebit,currentMonth,currentDay);
                                break;
                            case 1:
                                setChartMonthView(monthsCredit,monthsDebit,currentMonth);
                                break;
                            case 2:
                                setChartYearView(yearsCredit,yearsDebit, finalStartingYear,currentYear);
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getBalance() {

        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.fromto_rv);
        final MeRecyclerAdapter adapter = new MeRecyclerAdapter(this, otherMembersList, Currency.getInstance(customCurrencyCode).getSymbol(), exchangeRate);
        mRecyclerView.setAdapter(adapter);

        LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
        mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManagerVertical);

        double debito = 0d;
        double credito = 0d;

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
                    alertDialog = new AlertDialog.Builder(MeActivity.this)
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

                                            final DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference().child("notifications").child(groupId);
                                            final String notificationId = notificationRef.push().getKey();

                                            String username = mAuth.getCurrentUser().getDisplayName();

                                            if (username == null)
                                                username = "User";

                                            Calendar c = Calendar.getInstance();
                                            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy-HH-mm", Locale.getDefault());
                                            final String formattedDate = df.format(c.getTime());


                                            HashMap<String, Object> notification = new HashMap<>();

                                            notification.put("activity", getString(R.string.notififcationDenyPayedDebtActivity));
                                            notification.put("data", formattedDate);
                                            notification.put("id", groupId);
                                            notification.put("uid", userId);
                                            notification.put("groupId", groupId);
                                            notification.put("uname", username);

                                            notificationRef.child(notificationId).updateChildren(notification);

                                            DatabaseReference myNotRef = FirebaseDatabase.getInstance().getReference().child("utenti").child(userId).child("gruppi").child(groupId).child("notifiche");
                                            myNotRef.setValue(notificationId);

                                            /* Vale
                                             * Per visualizzare subito il valore aggiornato senza aspettare Firebase
                                             */
                                            subtractCredit(balance.getAmount());
                                            balance.setAmount((double) 0);
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


        if (debito < 0)
            debito = -debito;
        debito_tv.setText(String.format(Locale.getDefault(), "%.2f", debito * exchangeRate) + " " + Currency.getInstance(customCurrencyCode).getSymbol());

        credito_tv.setText(String.format(Locale.getDefault(), "%.2f", credito * exchangeRate) + " " + Currency.getInstance(customCurrencyCode).getSymbol());
    }

    private void setChartYearView(HashMap<Integer, Float> yearsCredit, HashMap<Integer, Float> yearsDebit, int startingYear, int endingYear)
    {
        chart.setData(null);
        chart.notifyDataSetChanged();
        chart.invalidate();


        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        chart.setDescription(null);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(4);
        xAxis.setDrawGridLines(true);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelRotationAngle(270);
        xAxis.setAvoidFirstLastClipping(true);

        xAxis.setAxisMinimum(startingYear);
        xAxis.setAxisMaximum(endingYear);

        List<Entry> entries = new ArrayList<Entry>();
        List<Entry> entries2 = new ArrayList<Entry>();

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return ((int) value)+"";
            }
        });

        float currentCredit = 0;
        float currentDebit = 0;



        for(int i=startingYear-1;i<endingYear+1;i++)
        {

            if(yearsCredit.containsKey(i))
                currentCredit = yearsCredit.get(i);
            if(yearsDebit.containsKey(i))
                currentDebit =  yearsDebit.get(i);

            entries.add(new Entry(i,currentCredit));
            entries2.add(new Entry(i,currentDebit));
        }

        LineDataSet creditoSet = new LineDataSet(entries, getResources().getString(R.string.credit));
        creditoSet.setColor(getResources().getColor(R.color.colorPrimary));
        creditoSet.setDrawCircles(false);
        creditoSet.setDrawValues(false);
        creditoSet.setLineWidth(2);
        LineDataSet debitoSet = new LineDataSet(entries2,getResources().getString(R.string.debit));
        debitoSet.setDrawCircles(false);
        debitoSet.setDrawValues(false);
        debitoSet.setLineWidth(2);
        debitoSet.setColor(getResources().getColor(R.color.redMaterial));

        LineData Data = new LineData(creditoSet,debitoSet);
        chart.setData(Data);
        chart.invalidate();
    }

    private void setChartMonthView(HashMap<Integer, Float> monthsCredit, HashMap<Integer, Float> monthsDebit, int currentMonth)
    {
        chart.setData(null);
        chart.notifyDataSetChanged();
        chart.invalidate();

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        chart.setDescription(null);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(11);
        xAxis.setDrawGridLines(true);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelRotationAngle(270);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                switch((int)value)
                {
                    case 1:
                        return getResources().getString(R.string.january);
                    case 2:
                        return getResources().getString(R.string.february);
                    case 3:
                        return getResources().getString(R.string.march);
                    case 4:
                        return getResources().getString(R.string.april);
                    case 5:
                        return getResources().getString(R.string.may);
                    case 6:
                        return getResources().getString(R.string.june);
                    case 7:
                        return getResources().getString(R.string.july);
                    case 8:
                        return getResources().getString(R.string.august);
                    case 9:
                        return getResources().getString(R.string.september);
                    case 10:
                        return getResources().getString(R.string.october);
                    case 11:
                        return getResources().getString(R.string.november);
                    case 12:
                        return getResources().getString(R.string.december);

                    default:
                        return value+"";

                }
            }
        });

        xAxis.setAxisMinimum(1);
        xAxis.setAxisMaximum(12);

        List<Entry> entries = new ArrayList<Entry>();
        List<Entry> entries2 = new ArrayList<Entry>();

        float currentCredit = 0;
        float currentDebit = 0;

        for(int i=1;i<currentMonth+1;i++)
        {

            if(monthsCredit.containsKey(i))
                currentCredit = monthsCredit.get(i);
            if(monthsDebit.containsKey(i))
                currentDebit =  monthsDebit.get(i);

            entries.add(new Entry(i,currentCredit));
            entries2.add(new Entry(i,currentDebit));
        }

        LineDataSet creditoSet = new LineDataSet(entries, getResources().getString(R.string.credit));
        creditoSet.setColor(getResources().getColor(R.color.colorPrimary));
        creditoSet.setDrawCircles(false);
        creditoSet.setDrawValues(false);
        creditoSet.setLineWidth(2);
        LineDataSet debitoSet = new LineDataSet(entries2,getResources().getString(R.string.debit));
        debitoSet.setDrawCircles(false);
        debitoSet.setDrawValues(false);
        debitoSet.setLineWidth(2);
        debitoSet.setColor(getResources().getColor(R.color.redMaterial));

        LineData Data = new LineData(creditoSet,debitoSet);
        chart.setData(Data);
        chart.invalidate();
    }

    private void setChartDayView(HashMap<Integer, Float> daysCredit, HashMap<Integer, Float> daysDebit, int currentMonth, int currentDay)
    {
        int monthDays = 31;

        if(currentMonth==11 || currentMonth==4 || currentMonth==9 || currentMonth==6)
            monthDays = 30;

        if(currentMonth==2)
            monthDays=28;


        chart.setData(null);
        chart.notifyDataSetChanged();
        chart.invalidate();

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        chart.setDescription(null);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(monthDays);
        xAxis.setDrawGridLines(true);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelRotationAngle(270);
        xAxis.setAvoidFirstLastClipping(true);

        xAxis.setAxisMinimum(1);
        xAxis.setAxisMaximum(monthDays);

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return ((int) value)+"";
            }
        });

        List<Entry> entries = new ArrayList<Entry>();
        List<Entry> entries2 = new ArrayList<Entry>();

        float currentCredit = 0;
        float currentDebit = 0;

        for(int i=1;i<currentDay+1;i++)
        {

            if(daysCredit.containsKey(i))
                currentCredit = daysCredit.get(i);
            if(daysDebit.containsKey(i))
                currentDebit =  daysDebit.get(i);

            entries.add(new Entry(i,currentCredit));
            entries2.add(new Entry(i,currentDebit));
        }

        LineDataSet creditoSet = new LineDataSet(entries, getResources().getString(R.string.credit));
        creditoSet.setColor(getResources().getColor(R.color.colorPrimary));
        creditoSet.setDrawCircles(false);
        creditoSet.setDrawValues(false);
        creditoSet.setLineWidth(2);
        LineDataSet debitoSet = new LineDataSet(entries2,getResources().getString(R.string.debit));
        debitoSet.setDrawCircles(false);
        debitoSet.setDrawValues(false);
        debitoSet.setLineWidth(2);
        debitoSet.setColor(getResources().getColor(R.color.redMaterial));

        LineData Data = new LineData(creditoSet,debitoSet);
        chart.setData(Data);
        chart.invalidate();


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

        if(alertDialog != null)
            if(alertDialog.isShowing())
                alertDialog.dismiss();

        if (netChange != null) {
            netChange.closeSnack();
            unregisterReceiver(netChange);
            netChange = null;
            Log.d("Receiver", "unregister on pause");
        }

    }

    public void subtractCredit(Double amount) {
        String fields[] = credito_tv.getText().toString().split(" ");
        credito_tv.setText(String.format(Locale.getDefault(), "%.2f", Float.valueOf(fields[0].replace(",", ".")) - amount) + " " + Currency.getInstance(Locale.ITALY).getSymbol());
    }
}
