package it.polito.mad.team19.mad_expenses;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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

public class GroupInfoActivity extends AppCompatActivity implements DeleteMemberDialog.NoticeDialogListener {

    private static final int GROUP_QUITTED = 99;
    ImageView image;
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbar;
    private FirebaseAuth mAuth;
    private String uid;
    private Boolean isUsrAdmin;
    RecyclerView members_lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        image = (ImageView) findViewById(R.id.group_info_toolbar_image_iv);
        toolbar = (Toolbar) findViewById(R.id.group_info_tb);

        String imageUrl = getIntent().getStringExtra("groupImage");
        String groupName = getIntent().getStringExtra("groupName");
        final String groupId = getIntent().getStringExtra("groupId");

        //setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle(groupName.toString());
        Log.d("DebugGroupInfo",groupName);


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
            Log.e("GroupInfoActivity", "Exception:\n" + e.toString());
        }



        //LUDO: membri


        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("gruppi").child(groupId).child("membri");

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        //Jured: controllo per sapere se l'utente è admin del gruppo
        final DatabaseReference isUserAdminRef = database.getReference("gruppi").child(groupId)
                .child("membri").child(uid).child("tipo");
        isUserAdminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("DebugDeleteMember",dataSnapshot.getValue().toString());
                if(Integer.valueOf(dataSnapshot.getValue().toString()).compareTo(1) == 0) {
                    isUsrAdmin = true;
                } else
                    isUsrAdmin = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        members_lv = (RecyclerView) findViewById(R.id.members_lv);
        final ArrayList<FirebaseGroupMember> contributors = new ArrayList<>();
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
                    Log.d("Contributors","no other members in the group!");
                else
                {
                    Log.d("no",contributors.toString());
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        adapter.SetOnItemLongClickListener(new GroupMembersRecyclerAdapter.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(View v, int position) {
                if(isUsrAdmin) {
                    DialogFragment newFragment = new DeleteMemberDialog();
                    Bundle selectedUid = new Bundle();

                    selectedUid.putString("currentUid",uid);
                    selectedUid.putString("selectedUid",contributors.get(position).getUid());
                    selectedUid.putString("groupId",groupId);
                    selectedUid.putString("nextAdminId",contributors.get((position + 1)%contributors.size()).getUid());
                    Log.d("DebugNextAdmin", String.valueOf((position + 1)%contributors.size()));
                    selectedUid.putString("usersLeft", String.valueOf(contributors.size()));
                    newFragment.setArguments(selectedUid);
                    newFragment.show(getSupportFragmentManager(), "DeleteDialog");
                }
                return false;
            }
        });

    }

    // TODO Jured: aggiungere tutti i controlli immaginabili sull'eliminazione di un utente dal gruppo
    //Se ha ancora debiti attivi non di può togliere
    //TODO aggiungere la possibilità di eliminarsi dal gruppo con restrizioni in caso di debiti attivi
    @Override
    public void onDialogDeleteMemberClick(DialogFragment dialog) {
        Log.d("DebugDialogClick","eliminare membro: " + dialog.getArguments().getString("selectedUid") + "  " + dialog.getArguments().getString("groupId"));
        String userToDelete = dialog.getArguments().getString("selectedUid");
        String groupId = dialog.getArguments().getString("groupId");
        String nexAdminId = dialog.getArguments().getString("nextAdminId");

        //rimuovo i due riferimenti tra gruppo e membro
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userToDeleteRef = database.getReference().child("gruppi").child(groupId)
                .child("membri").child(userToDelete);
        userToDeleteRef.removeValue();
        userToDeleteRef = database.getReference().child("utenti").child(userToDelete)
                .child("gruppi").child(groupId);
        userToDeleteRef.removeValue();

        //se mi autoelimino aggiorno l'admin
        if(userToDelete.compareTo(uid.toString())==0) {
            database.getReference().child("gruppi").child(groupId)
                    .child("membri").child(nexAdminId).child("tipo").setValue("1");
            //TODO Jured: se esco dal gruppo devo tornare alla groupListActivity
            Log.d("DebugGroupQuitted","GROUP_QUITTED result set");
            setResult(GROUP_QUITTED);
            dialog.getActivity().finish();
            //Intent intent = new Intent(dialog.getActivity(), GroupActivity.class);
            //startActivity(intent);
        }

    }

    @Override
    public void onDialogLeaveAndDeleteClick(DialogFragment dialog) {

        //chiamo il metodo per eliminare l'ultimo membro (che sarò io)
        onDialogDeleteMemberClick(dialog);

        Log.d("DebugDeleteMember", "onDialogLeaveAndDeleteClick");
        String groupId = dialog.getArguments().getString("groupId");

        //elimino il gruppo
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference groupToDeleteRef = database.getReference().child("gruppi").child(groupId);
        groupToDeleteRef.removeValue();
    }
}
