package it.polito.mad.team19.mad_expenses;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
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
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
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
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import it.polito.mad.team19.mad_expenses.Adapters.GroupMembersRecyclerAdapter;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseExpense;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;
import it.polito.mad.team19.mad_expenses.Classes.NetworkChangeReceiver;
import it.polito.mad.team19.mad_expenses.Classes.Notifications;
import it.polito.mad.team19.mad_expenses.Dialogs.DeleteMemberDialog;
import it.polito.mad.team19.mad_expenses.Dialogs.GalleryOrCameraDialog;
import it.polito.mad.team19.mad_expenses.Dialogs.ModifyGroupNameOrImageDialog;

public class GroupInfoActivity extends AppCompatActivity implements DeleteMemberDialog.NoticeDialogListener,
        ModifyGroupNameOrImageDialog.NoticeDialogListener, GalleryOrCameraDialog.NoticeDialogListener {

    private static final int GROUP_QUITTED = 99;
    private static final int REQUEST_GALLERY_IMAGE = 23;
    private static final int REQUEST_TAKE_PHOTO = 17;


    ImageView image;
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbar;
    private FirebaseAuth mAuth;
    private String uid;
    private Boolean isUsrAdmin;

    private String groupId;

    private String mCurrentPhotoPath = null;
    private String mCurrentPhotoName;
    private Uri mCurrentPhotoFirebaseUri;


    RecyclerView members_lv;

    ArrayList<FirebaseGroupMember> contributors;

    AlertDialog alertDialog = null;
    CardView leaveGroup_cw;

    NetworkChangeReceiver netChange;
    IntentFilter filter;
    String imageUrl;
    String groupName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netChange = new NetworkChangeReceiver();
        netChange.setViewForSnackbar(findViewById(android.R.id.content));
        netChange.setDialogShowTrue(false);
        registerReceiver(netChange, filter);

        image = (ImageView) findViewById(R.id.group_info_toolbar_image_iv);
        toolbar = (Toolbar) findViewById(R.id.group_info_tb);
        leaveGroup_cw = (CardView) findViewById(R.id.leaveGroup);

        imageUrl = getIntent().getStringExtra("groupImage");
        groupName = getIntent().getStringExtra("groupName");
        groupId = getIntent().getStringExtra("groupId");

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

                    if(child.child("immagine").exists())
                        contributors.add(new FirebaseGroupMember(child.child("nome").getValue().toString(),child.child("immagine").getValue().toString(),child.getKey()));
                    else
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
                    selectedUid.putString("selectedUsername",contributors.get(position).getName());
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
            case R.id.modify_group_details: {
                DialogFragment newFragment = new ModifyGroupNameOrImageDialog();
                newFragment.show(getSupportFragmentManager(), "modifyGroupDialog");
            }

            default:
                Log.e("ExpenseDetailsActivity", "Not finding a corresponding case to the menu item selected (" + id + ")");
                return super.onOptionsItemSelected(item);
        }
    }


    public void setNotification(final String groupId, final String userID, String username)
    {
        final DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference().child("notifications").child(groupId);
        final String notificationId = notificationRef.push().getKey();


        if(username==null)
            username="User";

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        final String formattedDate = df.format(c.getTime());

        HashMap<String, Object> notification = new HashMap<>();

        notification.put("activity", getString(R.string.notififcationRemoveMembersToGroupActivity));

        notification.put("data", formattedDate);
        notification.put("id", groupId);
        notification.put("uid", userID);
        notification.put("uname", username);
        notification.put("GroupImage", imageUrl);
        notification.put("GroupName", groupName);

        notificationRef.child(notificationId).updateChildren(notification);

        DatabaseReference myNotRef = FirebaseDatabase.getInstance().getReference().child("utenti").child(userID).child("gruppi").child(groupId).child("notifiche");
        myNotRef.setValue(notificationId);

    }


    private void setListenerLeaveGroup(final String groupId)
    {
        leaveGroup_cw.setVisibility(View.VISIBLE);

        leaveGroup_cw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = mAuth.getCurrentUser().getDisplayName();
                if(username==null)
                    username = "user";

                final String userID = mAuth.getCurrentUser().getUid();

                setNotification(groupId,userID,username);

                alertDialog = new AlertDialog.Builder(GroupInfoActivity.this)
                        .setTitle(R.string.confirmLeaveGroupTitle)
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
        String usernameToDelete = dialog.getArguments().getString("selectedUsername");
        String groupId = dialog.getArguments().getString("groupId");
        String nexAdminId = dialog.getArguments().getString("nextAdminId");
        setNotification(groupId,userToDelete,usernameToDelete);


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

    protected void onResume() {
        super.onResume();
        if (netChange == null) {
            netChange = new NetworkChangeReceiver();
            netChange.setViewForSnackbar(findViewById(android.R.id.content));
            netChange.setDialogShowTrue(false);
            registerReceiver(netChange, filter);
            Log.d("Receiver", "register on resum");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(alertDialog.isShowing())
            alertDialog.dismiss();

        if (netChange != null) {
            netChange.closeSnack();
            unregisterReceiver(netChange);
            netChange = null;
            Log.d("Receiver", "unregister on pause");
        }

    }

    @Override
    public void onModifyNameClick(DialogFragment dialog) {

        final String groupId = getIntent().getStringExtra("groupId");
        final String old_string = collapsingToolbar.getTitle().toString();

        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialogboxlayout_edit_account, null);

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
                            Intent intent = new Intent();
                            intent.putExtra("newGroupName",new_string.getText().toString());
                            setResult(RESULT_OK,intent);
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

    @Override
    public void onModifyImageClick(DialogFragment dialog) {
        DialogFragment newFragment = new GalleryOrCameraDialog();
        newFragment.show(getSupportFragmentManager(), "imageDialog");
    }

    @Override
    public void onDialogCameraClick(DialogFragment dialog) {
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        File photoFile;
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "it.polito.mad.team19.mad_expenses.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }


    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onDialogGalleryClick(DialogFragment dialog) {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_IMAGE);
    }

    private void uploadInfos() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("gruppi").child(groupId).child("immagine");
        String uuid = myRef.push().getKey();
        String idExpense = uuid;
        final DatabaseReference newExpenseRef = myRef.child(uuid);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference();
        final StorageReference groupImagesRef;

        if (mCurrentPhotoPath != null) {
            groupImagesRef = storageRef.child("images").child(groupId);
            File imageToUpload = new File(mCurrentPhotoPath);
            Bitmap fileBitmap = shrinkBitmap(mCurrentPhotoPath, 1000, 1000);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            fileBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            byte[] datas = baos.toByteArray();
            mCurrentPhotoName = imageToUpload.getName();
            UploadTask uploadTask = groupImagesRef.child(mCurrentPhotoName).putBytes(datas);
            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    // mCurrentPhotoFirebaseUri = taskSnapshot.getDownloadUrl();
                    Log.d("DebugCaricamentoImg", "caricamento immagine" + groupId +storageRef.getPath());
                    groupImagesRef.child(mCurrentPhotoName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            myRef.setValue(uri.toString());
                            final String imageUriTemp = new String(uri.toString());
                            DatabaseReference memberRef = FirebaseDatabase.getInstance().getReference()
                                    .child("gruppi").child(groupId).child("membri");
                            memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("utenti").child(data.getKey()).child("gruppi").child(groupId)
                                                .child("immagine").setValue(imageUriTemp.toString());
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                            mCurrentPhotoFirebaseUri = Uri.EMPTY;
                        }
                    });
                }
            });
        }
    }

    private Bitmap shrinkBitmap(String file, int width, int height) {

        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        Bitmap bitmap;
        BitmapFactory.decodeFile(file, bmpFactoryOptions); // Vale: No need to store the bitmap in the dedicated variable, I'm just loading its infos

        int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / (float) height);
        int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (float) width);

        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio)
                bmpFactoryOptions.inSampleSize = heightRatio;
            else
                bmpFactoryOptions.inSampleSize = widthRatio;
        }

        bmpFactoryOptions.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GALLERY_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImage = data.getData();
                Log.d("DebugGalleryImage:", selectedImage.getPath());
                String[] projection = {MediaStore.Images.Media.DATA};
                @SuppressWarnings("deprecation")
                Cursor cursor = managedQuery(selectedImage, projection, null, null, null);
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                mCurrentPhotoPath = cursor.getString(column_index);
                Log.d("DebugGalleryImage:2", mCurrentPhotoPath);
                //setImageView(mCurrentPhotoPath);
                uploadInfos();
            }
        }

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK)
            uploadInfos();
    }
}
