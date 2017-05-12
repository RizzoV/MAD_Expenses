package it.polito.mad.team19.mad_expenses;

import android.content.DialogInterface;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Adapters.GroupMembersRecyclerAdapter;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;

import static android.R.attr.data;
import static android.R.attr.type;
import static it.polito.mad.team19.mad_expenses.R.string.email;

public class GroupInfoActivity extends AppCompatActivity implements DeleteMemberDialog.NoticeDialogListener {

    private static final int GROUP_QUITTED = 99;
    ImageView image;
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbar;
    private FirebaseAuth mAuth;
    private String uid;
    private Boolean isUsrAdmin;
    RecyclerView members_lv;

    ArrayList<FirebaseGroupMember> contributors;

    AlertDialog alertDialog = null;
    CardView leaveGroup_cw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        image = (ImageView) findViewById(R.id.group_info_toolbar_image_iv);
        toolbar = (Toolbar) findViewById(R.id.group_info_tb);
        leaveGroup_cw = (CardView) findViewById(R.id.leaveGroup);

        String imageUrl = getIntent().getStringExtra("groupImage");
        String groupName = getIntent().getStringExtra("groupName");
        final String groupId = getIntent().getStringExtra("groupId");

       Log.d("DebugGroupInfo",groupName);

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.group_info_ctb);
        collapsingToolbar.setTitle(groupName);
        collapsingToolbar.setTitle(groupName.toString());

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Non crasha se non trova l'immagine del gruppo
        try {
            Glide.with(this).load(imageUrl).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().into(new BitmapImageViewTarget(image) {
                @Override
                protected void setResource(Bitmap resource) {
                    image.setImageDrawable(new BitmapDrawable(getResources(), resource));
                }
            });
        }
        catch (Exception e) {
            Log.e("GroupInfoActivity", "Exception:\n" + e.toString());
        }




        //LUDO: membri
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("gruppi").child(groupId).child("membri");

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        //Jured: controllo per sapere se l'utente è admin del gruppo
        final DatabaseReference isUserAdminRef = database.getReference("gruppi").child(groupId).child("membri").child(uid).child("tipo");
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
                Log.e("GroupInfoActivity", "Could not retrieve the user type");
            }
        });

        members_lv = (RecyclerView) findViewById(R.id.members_lv);
        contributors = new ArrayList<>();
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

                setListenerLeaveGroup(groupId);
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
                    selectedUid.putString("nextAdminId",getNextAdmin(uid,contributors));
                    selectedUid.putString("usersLeft", String.valueOf(contributors.size()));
                    newFragment.setArguments(selectedUid);
                    newFragment.show(getSupportFragmentManager(), "DeleteDialog");
                }
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_modify_group_name, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        final String groupId = getIntent().getStringExtra("groupId");
        final String old_string = collapsingToolbar.getTitle().toString();
        switch (id) {
            case R.id.modify_group_name: {
                LayoutInflater inflater = getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialogboxlayout_editaccount, null);
                final EditText new_string;

                new_string = (EditText) dialogView.findViewById(R.id.new_string);

                new_string.setText(old_string);

                final AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setTitle(R.string.modify_group_name)
                        .setPositiveButton(getString(R.string.edit), null)
                        .setNegativeButton(getString(R.string.cancel), null)
                        .create();

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button buttonPositive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        buttonPositive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (new_string.getText().toString().trim().isEmpty()) {
                                    new_string.setError(getString(R.string.mandatory_field));
                                } else {
                                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference groupNameRef = database.getReference()
                                            .child("gruppi").child(groupId).child("nome");
                                    groupNameRef.setValue(new_string.getText().toString());

                                    //TODO: cambiare il nome in tutti gli utenti
                                    DatabaseReference userGroupNameRef = database.getReference().child("gruppi").child(groupId)
                                            .child("membri");
                                    userGroupNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                                database.getReference().child("utenti").child(data.getKey()).child("gruppi")
                                                        .child(groupId).child("nome").setValue(new_string.getText().toString());
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                    collapsingToolbar.setTitle(new_string.getText().toString());
                                    dialog.dismiss();

                                }
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

            default:
                Log.e("ExpenseDetailsActivity", "Not finding a corresponding case to the menu item selected (" + id + ")");
                return super.onOptionsItemSelected(item);
        }
    }


    private void setListenerLeaveGroup(final String groupId)
    {
        leaveGroup_cw.setVisibility(View.VISIBLE);

        leaveGroup_cw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog = new AlertDialog.Builder(GroupInfoActivity.this)
                        .setMessage(R.string.confirmLeaveGroup)
                        .setPositiveButton(getString(R.string.yes), null)
                        .setNegativeButton(getString(R.string.no), null)
                        .create();

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button buttonPositive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        buttonPositive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                leaveGroup(uid,groupId,getNextAdmin(uid,contributors));
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
        });
    }

    private String getNextAdmin(String myUid,ArrayList<FirebaseGroupMember> contributors)
    {
        boolean found = false;
        int index = 0;

        for(int i=0; i<contributors.size() && !found;i++)
        {
            if(!contributors.get(i).getUid().equals(myUid)) {
                found = true;
                index = i;
            }
        }

        if(found)
            return contributors.get(index).getUid();
        else
            return null;
    }

    private void leaveGroup(String userToDelete,String groupId, String nexAdminId)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userToDeleteRef = database.getReference().child("gruppi").child(groupId)
                .child("membri").child(userToDelete);
        userToDeleteRef.removeValue();
        userToDeleteRef = database.getReference().child("utenti").child(userToDelete)
                .child("gruppi").child(groupId);
        userToDeleteRef.removeValue();

        //se mi autoelimino aggiorno l'admin
        if(userToDelete.compareTo(uid.toString())==0) {
            if(nexAdminId!=null)
            database.getReference().child("gruppi").child(groupId)
                    .child("membri").child(nexAdminId).child("tipo").setValue("1");
            else
            {
                DatabaseReference groupToDeleteRef = database.getReference().child("gruppi").child(groupId);
                groupToDeleteRef.removeValue();
            }
            //TODO Jured: se esco dal gruppo devo tornare alla groupListActivity
            Log.d("DebugGroupQuitted","GROUP_QUITTED result set");
            setResult(GROUP_QUITTED);
            finish();
            //Intent intent = new Intent(dialog.getActivity(), GroupActivity.class);
            //startActivity(intent);
        }
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
            if(nexAdminId!=null)
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
