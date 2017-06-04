package it.polito.mad.team19.mad_expenses;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.polito.mad.team19.mad_expenses.Adapters.ExpensesRecyclerAdapter;

public class CategoryPopupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String usrId; //credo non serva
    private String groupId;
    private String expenseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_popup);

        mAuth = FirebaseAuth.getInstance();

        usrId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        groupId = getIntent().getStringExtra("groupId");
        expenseId = getIntent().getStringExtra("expenseId");

        // Non occupare tutto lo schermo
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .95), (int) (height * .9));

        ImageView transport_btn;
        ImageView house_btn;
        ImageView food_btn;
        ImageView drink_btn;
        ImageView shopping_btn;
        ImageView other_btn;

        transport_btn = (ImageView) findViewById(R.id.transport_icon);
        house_btn = (ImageView) findViewById(R.id.house_icon);
        food_btn = (ImageView) findViewById(R.id.food_icon);
        drink_btn = (ImageView) findViewById(R.id.drink_icon);
        shopping_btn = (ImageView) findViewById(R.id.shopping_icon);
        other_btn = (ImageView) findViewById(R.id.other_icon);

        // In base a che icona si clicca viene salvato su Firebase il nome relativo all'icona
        // per poi riconoscerla quando la si tira giù dall ExpenseRecyclerAdapter
        // e settare quindi la giusta icona, visibile soltanto nell ExpenseList

        transport_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference newMessageRef;

                newMessageRef = database.getReference().child("gruppi").child(groupId).child("expenses").child(expenseId).child("category");
                newMessageRef.setValue("transport");
            }
        });

        house_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference newMessageRef;

                newMessageRef = database.getReference().child("gruppi").child(groupId).child("expenses").child(expenseId).child("category");
                newMessageRef.setValue("house");
            }
        });

        food_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference newMessageRef;

                newMessageRef = database.getReference().child("gruppi").child(groupId).child("expenses").child(expenseId).child("category");
                newMessageRef.setValue("food");
            }
        });

        drink_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference newMessageRef;

                newMessageRef = database.getReference().child("gruppi").child(groupId).child("expenses").child(expenseId).child("category");
                newMessageRef.setValue("drink");
            }
        });

        shopping_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference newMessageRef;

                newMessageRef = database.getReference().child("gruppi").child(groupId).child("expenses").child(expenseId).child("category");
                newMessageRef.setValue("shopping");
            }
        });

        other_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference newMessageRef;

                newMessageRef = database.getReference().child("gruppi").child(groupId).child("expenses").child(expenseId).child("category");
                newMessageRef.setValue("other");
            }
        });
    }
}
