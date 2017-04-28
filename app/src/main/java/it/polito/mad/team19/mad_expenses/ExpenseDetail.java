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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ExpenseDetail extends AppCompatActivity {

    TextView expense_name;
    TextView expense_desc;
    TextView expense_cost;
    TextView expense_author;
    ImageView expense_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_detail);
        setTitle("Dettagli Spesa");
        getSupportActionBar().setHomeButtonEnabled(true);

        String name= getIntent().getStringExtra("ExpenseName");
        String desc= getIntent().getStringExtra("ExpenseDesc");
        String imgUrl= getIntent().getStringExtra("ExpenseImgUrl");
        String cost= getIntent().getStringExtra("ExpenseCost");
        String authorId = getIntent().getStringExtra("ExpenseAuthorId");

        expense_name = (TextView) findViewById(R.id.expense_name);
        expense_desc = (TextView) findViewById(R.id.expense_description);
        expense_cost = (TextView) findViewById(R.id.expense_cost);
        expense_img = (ImageView) findViewById(R.id.expense_photo);
        expense_author = (TextView) findViewById(R.id.expense_author_value) ;


        expense_name.setText(name);
        expense_desc.setText(desc);
        expense_cost.setText(cost);
        expense_author.setText("loading...");

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("gruppi").child(getIntent().getStringExtra("groupId")).child("membri").child(authorId).child("nome").getRef();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("DebugFirebaseReading", "dentro");
                expense_author.setText(dataSnapshot.getValue().toString());
                if(!dataSnapshot.exists())
                    Log.e("DebugFirebaseReading", "not exists!");
                else
                    Log.e("DebugFirebaseReading", dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                expense_author.setText("-username cancelled-");
            }
        });


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
