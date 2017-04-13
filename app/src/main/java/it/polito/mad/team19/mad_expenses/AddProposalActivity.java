package it.polito.mad.team19.mad_expenses;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import com.google.firebase.storage.FirebaseStorage;

import java.util.UUID;

import it.polito.mad.team19.mad_expenses.Classes.FirebaseExpense;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseProposal;

/**
 * Created by Valentino on 04/04/2017.
 */

public class AddProposalActivity extends AppCompatActivity
{

    ImageButton imageButton;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseStorage storage;

    static final String COST_REGEX = "[0-9]+[.,]{0,1}[0-9]{0,2}";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_proposal);

        setTitle(R.string.create_new_proposal);

        //imageButton = (ImageButton) findViewById(R.id.image);

        storage = FirebaseStorage.getInstance();

        addListenerOnDoneButton();

    }

    public void addListenerOnDoneButton()
    {
        final String TAG = "firebaseAuth";

        FloatingActionButton doneBtn = (FloatingActionButton) findViewById(R.id.new_proposal_done_btn);
        doneBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                boolean empty = false;

                EditText nameEditText = (EditText) findViewById(R.id.new_proposal_name_et);
                EditText descriptionEditText = (EditText) findViewById(R.id.new_proposal_description_et);
                EditText costEditText = (EditText) findViewById(R.id.new_proposal_cost_et);

                if (TextUtils.isEmpty(nameEditText.getText().toString()))
                {
                    nameEditText.setError(getString(R.string.mandatory_field));
                    empty = true;
                }

                if (TextUtils.isEmpty(descriptionEditText.getText().toString()))
                {
                    descriptionEditText.setError(getString(R.string.mandatory_field));
                    empty = true;
                }

                //Jured: aggiunta validazione form inserimento costo (punto o virgola vanno bene per dividere intero da centesimi)
                if (TextUtils.isEmpty(costEditText.getText().toString()))
                {
                    costEditText.setError(getString(R.string.mandatory_field));
                    empty = true;
                }
                else if (!costEditText.getText().toString().matches(COST_REGEX))
                {
                    costEditText.setError(getString(R.string.invalid_cost_field));
                    empty = true;
                }

                if (!empty)
                {
                    mAuth = FirebaseAuth.getInstance();
                    mAuthListener = new FirebaseAuth.AuthStateListener()
                    {
                        @Override
                        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
                        {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null)
                            {
                                // User is signed in
                                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                            }
                            else
                            {
                                // User is signed out
                                Log.d(TAG, "onAuthStateChanged:signed_out");
                            }
                            // ...
                        }
                    };

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("proposals");
                    String uuid = UUID.randomUUID().toString();
                    DatabaseReference newProposalRef = myRef.child(uuid);
                    newProposalRef.setValue(new FirebaseProposal(nameEditText.getText().toString(), descriptionEditText.getText().toString(), Float.valueOf(costEditText.getText().toString().replace(",", ".")), "link_png"));

                /*DatabaseReference newExpenseNameRef = newExpenseRef.child("name");
                DatabaseReference newExpenseDescriptionRef = newExpenseRef.child("description");
                newExpenseNameRef.setValue(nameEditText.getText().toString());
                newExpenseDescriptionRef.setValue(nameEditText.getText().toString());*/

                    finish();
                }
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }



}
