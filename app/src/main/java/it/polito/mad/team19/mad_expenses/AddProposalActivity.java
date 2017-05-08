package it.polito.mad.team19.mad_expenses;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import it.polito.mad.team19.mad_expenses.Classes.FirebaseExpense;

/**
 * Created by Valentino on 04/04/2017.
 */

public class AddProposalActivity extends AppCompatActivity implements GalleryOrCameraDialog.NoticeDialogListener
{

    private static final int STORAGE_REQUEST = 666;
    ImageButton imageButton;

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
    StorageReference storageRef;
    StorageReference groupImagesRef;
    EditText nameEditText;
    EditText descriptionEditText;
    EditText costEditText;

    static final String COST_REGEX = "[0-9]+[.,]{0,1}[0-9]{0,2}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_proposal);

        mImageView = (ImageView) findViewById(R.id.camera_img);


        groupId = getIntent().getExtras().getString("groupId");
        usrId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setTitle(R.string.create_new_proposal);

        //imageButton = (ImageButton) findViewById(R.id.image);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

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
    }

    private void addListenerOnDoneButton() {

        final String TAG = "firebaseAuth";

        FloatingActionButton doneBtn = (FloatingActionButton) findViewById(R.id.new_proposal_done_btn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean empty = false;

                nameEditText = (EditText) findViewById(R.id.new_proposal_name_et);
                descriptionEditText = (EditText) findViewById(R.id.new_proposal_description_et);
                costEditText = (EditText) findViewById(R.id.new_proposal_cost_et);

                if (TextUtils.isEmpty(nameEditText.getText().toString())) {
                    nameEditText.setError(getString(R.string.mandatory_field));
                    empty = true;
                }

                if (TextUtils.isEmpty(descriptionEditText.getText().toString())) {
                    descriptionEditText.setError(getString(R.string.mandatory_field));
                    empty = true;
                }

                //Jured: aggiunta validazione form inserimento costo (punto o virgola vanno bene per dividere intero da centesimi)
                if (TextUtils.isEmpty(costEditText.getText().toString())) {
                    costEditText.setError(getString(R.string.mandatory_field));
                    empty = true;
                } else if (!costEditText.getText().toString().matches(COST_REGEX)) {
                    costEditText.setError(getString(R.string.invalid_cost_field));
                    empty = true;
                }

                if (!empty) {
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
                    //DatabaseReference myRef =
                    DatabaseReference myRef = database.getReference("gruppi").child(groupId).child("proposals");
                    String uuid = UUID.randomUUID().toString();
                    final DatabaseReference newProposalRef = myRef.child(uuid);

                    groupImagesRef = storageRef.child("images").child(groupId);

                    File imageToUpload = new File(mCurrentPhotoPath);

                    //TODO chiedere i permessi di accesso alla memoria (Marshmallow+)
                    //TODO contemplare il caso in cui non vi sia alcuna immagine allegata
                    Bitmap fileBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
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
                            groupImagesRef.child(mCurrentPhotoName).getDownloadUrl()

                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            Log.e("DebugUriRequest",uri.toString());
                                            newProposalRef.setValue(new FirebaseExpense(usrId,nameEditText.getText().toString(), descriptionEditText.getText().toString(),
                                                    Float.valueOf(costEditText.getText().toString().replace(",", ".")), uri.toString()));
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


                    //ADD TO REVERT
                    //uploadImageToFirebase(mCurrentPhotoPath);

                    //newExpenseRef.setValue(new FirebaseExpense(nameEditText.getText().toString(), descriptionEditText.getText().toString(),
                    //        Float.valueOf(costEditText.getText().toString().replace(",", ".")), "sample/link.png"));


                    setResult(EXP_CREATED);
                    finish();
                }
            }
        });
    }

    public void addListenerOnImageButton() {

        imageButton = (ImageButton) findViewById(R.id.new_expense_image_button);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //FOR EXAMPLE
                // Toast.makeText(MyAndroidAppActivity.this,"ImageButton is clicked!", Toast.LENGTH_SHORT).show();

                // TO REPLACE WITH THE CODE FOR THE UPLOAD OF THE IMAGE
                //Snackbar.make(view, "Replace with your image", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                //TO LOAD IMAGE FROM GALLERY (error with RESULT_LOAD_IMAGE)
                //Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //startActivityForResult(i, RESULT_LOAD_IMAGE);

                ////////////////////////////////////
                ////dispatchTakePictureIntent();////
                ////////////////////////////////////
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

        if(requestCode == REQUEST_TAKE_PHOTO) {

            Bitmap fileBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            mImageView.setImageBitmap(fileBitmap);


            //uploadImageToFirebase(mCurrentPhotoPath);

            /*
            File imageToUpload = new File(mCurrentPhotoPath);
            Uri file = Uri.fromFile(imageToUpload);


            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap fileBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath,bmOptions);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            fileBitmap.compress(Bitmap.CompressFormat.JPEG, 7, baos);
            byte[] datas = baos.toByteArray();
            mImageView.setImageBitmap(fileBitmap);
            UploadTask uploadTask = groupImagesRef.child(imageToUpload.getName()).putBytes(datas);
            */

            //UploadTask uploadTask = groupImagesRef.child(imageToUpload.getName()).putFile(file);

            Log.d("DEBUG APP: ", mCurrentPhotoPath);
        }

        if(requestCode == REQUEST_GALLERY_IMAGE){

            if (data != null) {

                Uri selectedImage = data.getData();

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();

                Log.e("DebugGalleryImage:", selectedImage.getPath());

                String[] projection = {MediaStore.Images.Media.DATA};
                @SuppressWarnings("deprecation")
                Cursor cursor = managedQuery(selectedImage, projection, null, null, null);
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                mCurrentPhotoPath = cursor.getString(column_index);

                Log.e("DebugGalleryImage2:", mCurrentPhotoPath);

                setImageView(mCurrentPhotoPath);


            }
            //uploadImageToFirebase(mCurrentPhotoPath);

            /*
                File imageToUpload = new File(selectedImagePath);

                Bitmap fileBitmap = BitmapFactory.decodeFile(selectedImagePath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                fileBitmap.compress(Bitmap.CompressFormat.JPEG, 7, baos);
                byte[] datas = baos.toByteArray();
                mImageView.setImageBitmap(fileBitmap);
                UploadTask uploadTask = groupImagesRef.child(imageToUpload.getName()).putBytes(datas);
                */

        }
    }

    private void setImageView(String mCurrentPhotoPath) {

        //TODO Bolz: fare la bitmap circolare
        Bitmap fileBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        mImageView.setImageBitmap(fileBitmap);
    }

    /*if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);

            StorageReference storageRef = storage.getReference();

            // Create a child reference
            // imagesRef now points to "images"
            StorageReference imagesRef = storageRef.child("images");

            // Child references can also take paths
            // spaceRef now points to "images/space.jpg
            // imagesRef still points to "images"
            StorageReference schcuntrinRef = storageRef.child("images/primoschcuntrin.jpg");

            // Get the data from an ImageView as bytes
            mImageView.setDrawingCacheEnabled(true);
            mImageView.buildDrawingCache();
            Bitmap bitmap = mImageView.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] datas = baos.toByteArray();

            UploadTask uploadTask = schcuntrinRef.putBytes(datas);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                }
            });
        }
    }*/

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
        File photoFile = null;
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

}
