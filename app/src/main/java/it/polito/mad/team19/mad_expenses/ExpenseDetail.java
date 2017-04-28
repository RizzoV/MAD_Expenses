package it.polito.mad.team19.mad_expenses;

import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ExpenseDetail extends AppCompatActivity {

    TextView expense_name;
    TextView expense_desc;
    TextView expense_cost;
    ImageView expense_img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_detail);
        setTitle("Dettagli Spesa");
        getSupportActionBar().setHomeButtonEnabled(true);

        //TODO: Estrarre varie stringhe

        String name= getIntent().getStringExtra("ExpenseName");
        String desc= getIntent().getStringExtra("ExpenseDesc");
        String imgUrl= getIntent().getStringExtra("ExpenseImgUrl");
        String cost= getIntent().getStringExtra("ExpenseCost");

        expense_name = (TextView) findViewById(R.id.expense_name);
        expense_desc = (TextView) findViewById(R.id.expense_description);
        expense_cost = (TextView) findViewById(R.id.expense_cost);
        expense_img = (ImageView) findViewById(R.id.expense_photo);

        expense_name.setText(name);
        expense_desc.setText(desc);
        expense_cost.setText(cost);


        //Jured: gestito il caso in cui arrivi un link che indica l'assenza di immagine
        if(imgUrl == null) {
            expense_img.setImageResource(R.drawable.circle);
        } else {
            Log.e("DebugExpenseDetails",imgUrl);

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
        }
    }
}
