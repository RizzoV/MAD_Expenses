package it.polito.mad.team19.mad_expenses;

import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOError;
import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Adapters.GroupMembersRecyclerAdapter;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;

public class GroupInfoActivity extends AppCompatActivity {

    ImageView image;
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbar;
    private FirebaseAuth mAuth;
    String uid;
    RecyclerView members_lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        image = (ImageView) findViewById(R.id.group_info_toolbar_image_iv);
        toolbar = (Toolbar) findViewById(R.id.group_info_tb);

        String imageUrl = getIntent().getStringExtra("groupImage");
        String groupName = getIntent().getStringExtra("groupName");
        String groupId = getIntent().getStringExtra("groupId");

        //setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle(groupName.toString());
        Log.e("DebugGroupInfo",groupName);


        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.group_info_ctb);
        collapsingToolbar.setTitle(groupName.toString());
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        //Non crasha se non trova l'iimagine del gruppo
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReferenceFromUrl(imageUrl);
            final long ONE_MEGABYTE = 1024 * 1024;
            storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    image.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0,bytes.length));
                    //Drawable drawable = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bytes, 0,bytes.length));
                    //getSupportActionBar().setBackgroundDrawable(drawable);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }
        catch (Exception e)
        {
            Log.e("Exception",e.toString());
        }



        //LUDO: membri


        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("gruppi").child(groupId).child("membri");

        mAuth = FirebaseAuth.getInstance();

        members_lv = (RecyclerView) findViewById(R.id.members_lv);
        final ArrayList<FirebaseGroupMember> contributors = new ArrayList<FirebaseGroupMember>();
        final GroupMembersRecyclerAdapter adapter = new GroupMembersRecyclerAdapter(this,contributors);
        members_lv.setAdapter(adapter);

        LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
        mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
        members_lv.setLayoutManager(mLinearLayoutManagerVertical);


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int nMembers = 0;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                        contributors.add(new FirebaseGroupMember(child.child("nome").getValue().toString(),null,child.getKey()));
                        nMembers++;
                }

                if(nMembers==0)
                    Log.e("Contributors","no other members in the group!");
                else
                {
                    Log.e("no",contributors.toString());
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
