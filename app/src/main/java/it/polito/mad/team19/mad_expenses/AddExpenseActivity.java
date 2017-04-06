package it.polito.mad.team19.mad_expenses;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.polito.mad.team19.mad_expenses.Classes.FirebaseExpense;

/**
 * Created by Bolz on 03/04/2017.
 */


public class AddExpenseActivity extends AppCompatActivity
{
    ImageButton imageButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    static final String COST_REGEX = "[0-9]+[.,]{0,1}[0-9]{0,2}";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        setTitle(R.string.create_new_expense);

        //imageButton = (ImageButton) findViewById(R.id.image);

        addListenerOnDoneButton();
        addListenerOnImageButton();

    }

    private void addListenerOnDoneButton() {

        final String TAG = "firebaseAuth";

        FloatingActionButton doneBtn = (FloatingActionButton) findViewById(R.id.new_expense_done_btn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean empty = false;

                EditText nameEditText = (EditText) findViewById(R.id.new_expense_name_et);
                EditText descriptionEditText = (EditText) findViewById(R.id.new_expense_description_et);
                EditText costEditText = (EditText) findViewById(R.id.new_expense_cost_et);

                if(TextUtils.isEmpty(nameEditText.getText().toString())) {
                    nameEditText.setError(getString(R.string.mandatory_field));
                    empty = true;
                }

                if(TextUtils.isEmpty(descriptionEditText.getText().toString())) {
                    descriptionEditText.setError(getString(R.string.mandatory_field));
                    empty = true;
                }

                if(TextUtils.isEmpty(costEditText.getText().toString())) {
                    costEditText.setError(getString(R.string.mandatory_field));
                    empty = true;
                }

                //Jured: aggiunta validazione form inserimento costo (punto o virgola vanno bene per dividere intero da centesimi)
                if(!costEditText.getText().toString().matches(COST_REGEX)) {
                    costEditText.setError(getString(R.string.invalid_cost_field));
                    empty = true;
                }

                if(!empty) {
                    mAuth = FirebaseAuth.getInstance();
                    mAuthListener = new FirebaseAuth.AuthStateListener() {


                        @Override
                        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                // User is signed in
                                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                            } else {
                                // User is signed out
                                Log.d(TAG, "onAuthStateChanged:signed_out");
                            }
                            // ...
                        }
                    };

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("expenses");
                    String uuid = UUID.randomUUID().toString();
                    DatabaseReference newExpenseRef = myRef.child(uuid);
                    newExpenseRef.setValue(new FirebaseExpense(nameEditText.getText().toString(), descriptionEditText.getText().toString(), Float.valueOf(costEditText.getText().toString().replace(",",".")), "link_png"));
                /*DatabaseReference newExpenseNameRef = newExpenseRef.child("name");
                DatabaseReference newExpenseDescriptionRef = newExpenseRef.child("description");
                newExpenseNameRef.setValue(nameEditText.getText().toString());
                newExpenseDescriptionRef.setValue(nameEditText.getText().toString());*/

                    finish();
                }
            }
        });
    }

    public void addListenerOnImageButton()
    {

        imageButton = (ImageButton) findViewById(R.id.image);

        imageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //FOR EXAMPLE
               // Toast.makeText(MyAndroidAppActivity.this,"ImageButton is clicked!", Toast.LENGTH_SHORT).show();

                // TO REPLACE WITH THE CODE FOR THE UPLOAD OF THE IMAGE
                Snackbar.make(view, "Replace with your image", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                //TO LOAD IMAGE FROM GALLERY (error with RESULT_LOAD_IMAGE)
                //Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //startActivityForResult(i, RESULT_LOAD_IMAGE);
            }

        });

        /*@Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            // String picturePath contains the path of selected Image
        }*/

    }


    @Override
    protected void onStart() {
        super.onStart();
        //mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


}
