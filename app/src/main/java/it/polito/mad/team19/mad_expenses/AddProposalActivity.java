package it.polito.mad.team19.mad_expenses;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Locale;
import java.util.Map;

import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;
import it.polito.mad.team19.mad_expenses.Classes.NetworkChangeReceiver;
import it.polito.mad.team19.mad_expenses.Classes.Notifications;
import it.polito.mad.team19.mad_expenses.Dialogs.GalleryOrCameraDialog;

/**
 * Created by Valentino on 04/04/2017.
 */

public class AddProposalActivity extends AppCompatActivity implements GalleryOrCameraDialog.NoticeDialogListener
{

    private static final int STORAGE_REQUEST = 666;
    private ImageButton imageButton;

    private ImageView mImageView;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseStorage storage;
    private String groupId;
    private String usrId;
    private String mCurrentPhotoPath;
    private String mCurrentPhotoName;
    private Uri mCurrentPhotoFirebaseUri;
    private static final int EXP_CREATED = 1;
    private StorageReference storageRef;
    private StorageReference groupImagesRef;
    private EditText nameEditText;
    private EditText descriptionEditText;
    private EditText costEditText;

    private String proposalId;

    private NetworkChangeReceiver netChange;
    private IntentFilter filter;

    static final String COST_REGEX = "[0-9]+[.,]{0,1}[0-9]{0,2}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_proposal);

        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netChange = new NetworkChangeReceiver();
        netChange.setViewForSnackbar(findViewById(android.R.id.content));
        netChange.setDialogShowTrue(false);
        registerReceiver(netChange, filter);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        mImageView = (ImageView) findViewById(R.id.camera_img);

        groupId = getIntent().getStringExtra("groupId");
        usrId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setTitle(R.string.create_new_proposal);

        // Set listeners on Done and Image buttons
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddProposalActivity.this,
                        new String[]{android.Manifest.permission.READ_CONTACTS},
                        STORAGE_REQUEST);
            } else {
                // The permission is granted, we can perform the action
                addListenerOnDoneButton();
                addListenerOnImageButton();
            }
        }
        else{
            addListenerOnDoneButton();
            addListenerOnImageButton();
        }
    }

    private void addListenerOnDoneButton() {

        final String TAG = "firebaseAuth";

        FloatingActionButton doneBtn = (FloatingActionButton) findViewById(R.id.new_proposal_done_btn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean invalidFields = false;

                nameEditText = (EditText) findViewById(R.id.new_proposal_name_et);
                descriptionEditText = (EditText) findViewById(R.id.new_proposal_description_et);
                costEditText = (EditText) findViewById(R.id.new_proposal_cost_et);

                if (TextUtils.isEmpty(nameEditText.getText().toString())) {
                    nameEditText.setError(getString(R.string.mandatory_field));
                    invalidFields = true;
                }

                //Jured: aggiunta validazione form inserimento costo (punto o virgola vanno bene per dividere intero da centesimi)
                if (TextUtils.isEmpty(costEditText.getText().toString())) {
                    costEditText.setError(getString(R.string.mandatory_field));
                    invalidFields = true;
                } else if (!costEditText.getText().toString().matches(COST_REGEX)) {
                    costEditText.setError(getString(R.string.invalid_cost_field));
                    invalidFields = true;
                }

                if (!invalidFields) {
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


                    Float proposalTotal = Float.parseFloat(costEditText.getText().toString().replace(",", "."));

                    uploadInfos();

                    setResult(EXP_CREATED);
                    finish();
                }
            }
        });
    }

    private void uploadInfos() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("gruppi").child(groupId).child("proposals");
        proposalId = myRef.push().getKey();

        AsyncFirebaseProposalLoader async = new AsyncFirebaseProposalLoader(proposalId, groupId, usrId, mCurrentPhotoPath, mCurrentPhotoName,
                nameEditText.getText().toString(), descriptionEditText.getText().toString(), costEditText.getText().toString(), this);

        async.execute();
    }

    public void finishTasks() {

        final DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference().child("notifications").child(groupId);
        final String notificationId = notificationRef.push().getKey();

        String username = mAuth.getCurrentUser().getDisplayName();

        if (username == null)
            username = "User";

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        final String formattedDate = df.format(c.getTime());

        HashMap<String, Object> notification = new HashMap<>();

        notification.put("activity", getString(R.string.notififcationAddProposalActivity));

        notification.put("data", formattedDate);
        notification.put("id", proposalId);
        notification.put("uid", usrId);
        notification.put("groupId", groupId);
        notification.put("uname", username);
        notification.put("ProposalName", nameEditText.getText().toString());
        notification.put("ProposalDesc", descriptionEditText.getText().toString());
        notification.put("ProposalCost", costEditText.getText().toString());

        notificationRef.child(notificationId).updateChildren(notification);

        DatabaseReference myNotRef = FirebaseDatabase.getInstance().getReference().child("utenti").child(usrId).child("gruppi").child(groupId).child("notifiche");
        myNotRef.setValue(notificationId);
    }

    public void addListenerOnImageButton() {

        imageButton = (ImageButton) findViewById(R.id.new_expense_image_button);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new GalleryOrCameraDialog();
                newFragment.show(getSupportFragmentManager(), "imageDialog");
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

    //Jured: aggiunto codice che scatta una foto, la salva su file e poi la carica
    //su firebase in modo totalmente ignorante, sempre alla stessa locazione e per ora senza compressione;


    static final int REQUEST_IMAGE_CAPTURE = 0;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_GALLERY_IMAGE = 2;

    //TODO check sul resultCode
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d("DEBUG AGGIUNTA FOTO: ", mCurrentPhotoPath);

        /////FIREBASE STORE
        StorageReference storageRef = storage.getReference();
        StorageReference groupImagesRef = storageRef.child("images").child(groupId);

        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (mCurrentPhotoPath != null) {
                Log.d("DebugTakePhoto2", mCurrentPhotoPath);
                setImageView(mCurrentPhotoPath);
            }
        }

        if (requestCode == REQUEST_GALLERY_IMAGE) {
            if (data != null) {
                Uri selectedImage = data.getData();
                Log.d("DebugGalleryImage", selectedImage.getPath());
                String[] projection = {MediaStore.Images.Media.DATA};
                @SuppressWarnings("deprecation")
                Cursor cursor = managedQuery(selectedImage, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                mCurrentPhotoPath = cursor.getString(column_index);
                Log.d("DebugGalleryImage2", mCurrentPhotoPath);
                setImageView(mCurrentPhotoPath);
            }
        }
    }

    private void setImageView(String mCurrentPhotoPath) {
        Bitmap fileBitmap = shrinkBitmap(mCurrentPhotoPath, 1000, 1000);
        mImageView.setImageBitmap(fileBitmap);
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

    private void uploadImageToFirebase(String filePath){

        groupImagesRef = storageRef.child("images").child(groupId);

        File imageToUpload = new File(filePath);

        //TODO chiedere i permessi di accesso alla memoria (Marshmallow+)
        Bitmap fileBitmap = BitmapFactory.decodeFile(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fileBitmap.compress(Bitmap.CompressFormat.JPEG, 7, baos);
        byte[] datas = baos.toByteArray();
        mImageView.setImageBitmap(fileBitmap);
        mCurrentPhotoName= imageToUpload.getName();
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
                /*mCurrentPhotoFirebaseUri = groupImagesRef.child(mCurrentPhotoName).getDownloadUrl().getResult();
                Log.e("DebugUriRequest",mCurrentPhotoFirebaseUri.toString());

                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // TODO: handle uri
                                Log.e("DebugUriRequest",uri.toString());
                                mCurrentPhotoFirebaseUri = uri;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        mCurrentPhotoFirebaseUri = Uri.EMPTY;
                    }
                });
                */
            }
        });

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

    @Override
    public void onDialogCameraClick(DialogFragment dialog) {
        dispatchTakePictureIntent();
    }

    @Override
    public void onDialogGalleryClick(DialogFragment dialog) {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , REQUEST_GALLERY_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case STORAGE_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    addListenerOnDoneButton();
                    addListenerOnImageButton();
                } else {
                    ActivityCompat.requestPermissions(AddProposalActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            STORAGE_REQUEST);

                }
                return;
        }

        // other 'case' lines to check for other
        // permissions this app might request
    }

    @Override
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

        if (netChange != null) {
            netChange.closeSnack();
            unregisterReceiver(netChange);
            netChange = null;
            Log.d("Receiver", "unregister on pause");
        }


    }

}
