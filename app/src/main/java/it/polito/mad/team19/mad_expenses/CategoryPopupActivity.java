package it.polito.mad.team19.mad_expenses;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.polito.mad.team19.mad_expenses.Adapters.ExpensesRecyclerAdapter;

public class CategoryPopupActivity extends Activity {

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

        LinearLayout transport_btn;
        LinearLayout house_btn;
        LinearLayout food_btn;
        LinearLayout drink_btn;
        LinearLayout shopping_btn;
        LinearLayout other_btn;

        transport_btn = (LinearLayout) findViewById(R.id.select_transport);
        house_btn = (LinearLayout) findViewById(R.id.select_house);
        food_btn = (LinearLayout) findViewById(R.id.select_food);
        drink_btn = (LinearLayout) findViewById(R.id.select_drink);
        shopping_btn = (LinearLayout) findViewById(R.id.select_shopping);
        other_btn = (LinearLayout) findViewById(R.id.select_other);

        // In base a che icona si clicca viene salvato su Firebase il nome relativo all'icona
        // per poi riconoscerla quando la si tira giù dall ExpenseRecyclerAdapter
        // e settare quindi la giusta icona, visibile soltanto nell ExpenseList

        transport_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("ExpenseThumb","transport");
                setResult(1,intent);
                finish();
            }
        });

        house_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("ExpenseThumb","house");
                setResult(1,intent);
                finish();
            }
        });

        food_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.putExtra("ExpenseThumb","food");
                setResult(1,intent);
                finish();
            }
        });

        drink_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.putExtra("ExpenseThumb","drink");
                setResult(1,intent);
                finish();
            }
        });

        shopping_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.putExtra("ExpenseThumb","shopping");
                setResult(1,intent);
                finish();
            }
        });

        other_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.putExtra("ExpenseThumb","other");
                setResult(1,intent);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
