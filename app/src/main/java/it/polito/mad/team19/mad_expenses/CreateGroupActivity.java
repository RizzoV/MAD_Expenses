package it.polito.mad.team19.mad_expenses;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class CreateGroupActivity extends AppCompatActivity {

    Button info_type_button;
    CheckBox distributed;
    CheckBox centralized;
    Button add_group;
    EditText group_name;
    Snackbar bar;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        getSupportActionBar().setTitle("Crea un nuovo gruppo");

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();



        Log.e("User",mAuth.getCurrentUser().getUid());

        distributed = (CheckBox) findViewById(R.id.type_1);
        centralized = (CheckBox) findViewById(R.id.type_2);
        info_type_button = (Button) findViewById(R.id.info_type_button);
        add_group = (Button) findViewById(R.id.add_group_submit);
        group_name = (EditText) findViewById(R.id.new_group_name);

        add_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean allset = true;
                int type;
                //aggiungere anche controllo su immagine
                if(group_name.getText().toString().isEmpty()){
                    allset = false;
                    group_name.setError("Devi inserire un nome!");
                }

                if(!distributed.isChecked() && !centralized.isChecked())
                {
                    bar = Snackbar.make(v, "Devi selezionare almeno un tipo!", Snackbar.LENGTH_LONG)
                            .setAction("Ok", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                   bar.dismiss();
                                }
                            });

                    bar.show();
                    allset = false;
                }

                if(distributed.isChecked())
                    type = 0;
                else
                    type = 1;


                if(allset)
                {
                    addGroupToFirebase(mAuth.getCurrentUser().getUid(),group_name.getText().toString(),"path/immmagine.png",type);
                }


            }
        });

        info_type_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateGroupActivity.this,GroupTypeInfoActivity.class);
                startActivity(intent);
            }
        });


        distributed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                centralized.setChecked(false);
            }
        });

        centralized.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                distributed.setChecked(false);
            }
        });


    }

    private void addGroupToFirebase(String uid, String name, String img, int type) {
        String groupid = mDatabase.child("gruppi").push().getKey();

        mDatabase.child("gruppi").child(groupid).child("immagine").setValue(img);
        mDatabase.child("gruppi").child(groupid).child("membri").child(uid).setValue(1);
        mDatabase.child("gruppi").child(groupid).child("nome").setValue(name);
        mDatabase.child("gruppi").child(groupid).child("tipo").setValue(type);
        mDatabase.child("gruppi").child(groupid).child("totale").setValue(0);
        mDatabase.child("gruppi").child(groupid).child("stato").setValue("created");

        mDatabase.child("utenti").child(uid).child("gruppi").child(groupid).child("bilancio").setValue(0);
        mDatabase.child("utenti").child(uid).child("gruppi").child(groupid).child("immagine").setValue(img);
        mDatabase.child("utenti").child(uid).child("gruppi").child(groupid).child("nome").setValue(name);
        mDatabase.child("utenti").child(uid).child("gruppi").child(groupid).child("notifiche").setValue(0);


        finish();

    }


}
