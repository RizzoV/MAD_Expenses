package it.polito.mad.team19.mad_expenses;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.net.Uri;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.polito.mad.team19.mad_expenses.Classes.FirebaseExpense;

/**
 * Created by Bolz on 03/04/2017.
 */


public class AddExpenseActivity extends AppCompatActivity implements GalleryOrCameraDialog.NoticeDialogListener {

    private ImageButton imageButton;
    private ImageView mImageView;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseStorage storage;
    static final String COST_REGEX = "[0-9]+[.,]{0,1}[0-9]{0,2}";
    private String groupId;
    Boolean isContributorsClicked = true;
    Boolean isExcludedClicked = true;
    private static final int EXP_CREATED = 1;
    private String mCurrentPhotoPath;
    private String mCurrentPhotoName;
    private Uri mCurrentPhotoFirebaseUri;
    StorageReference storageRef;
    StorageReference groupImagesRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        groupId = getIntent().getExtras().getString("groupId");

        setTitle(R.string.create_new_expense);

        //imageButton = (ImageButton) findViewById(R.id.image);
        mImageView = (ImageView) findViewById(R.id.camera_img);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        addListenerOnDoneButton();
        addListenerOnImageButton();

        addListenerOnContributorsButton();
        addListenerOnExcludedButton();

    }


    private void addListenerOnContributorsButton() {
        Button contributorsButton = (Button) findViewById(R.id.contributors_button);

        contributorsButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AddExpenseActivity.this, ContributorsPopupActivity.class);
                startActivity(i);
            }
        });
    }

    private void addListenerOnExcludedButton() {
        Button contributorsButton = (Button) findViewById(R.id.excluded_button);

        contributorsButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AddExpenseActivity.this, ExcludedPopupActivity.class);
                startActivity(i);
            }
        });
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
                    DatabaseReference myRef = database.getReference("expenses");
                    String uuid = UUID.randomUUID().toString();
                    DatabaseReference newExpenseRef = myRef.child(uuid);





                    uploadImageToFirebase(mCurrentPhotoPath);

                    newExpenseRef.setValue(new FirebaseExpense(nameEditText.getText().toString(), descriptionEditText.getText().toString(),
                            Float.valueOf(costEditText.getText().toString().replace(",", ".")), "sample/link.png"));



                /*DatabaseReference newExpenseNameRef = newExpenseRef.child("name");
                DatabaseReference newExpenseDescriptionRef = newExpenseRef.child("description");
                newExpenseNameRef.setValue(nameEditText.getText().toString());
                newExpenseDescriptionRef.setValue(nameEditText.getText().toString());*/
                    setResult(EXP_CREATED);
                    finish();
                }
            }
        });
    }

    public void addListenerOnImageButton() {

        imageButton = (ImageButton) findViewById(R.id.image);

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



                Uri selectedImage = data.getData();
                //final InputStream imageStream = getContentResolver().openInputStream(selectedImage);

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();

                Log.e("DebugGalleryImage:",selectedImage.getPath());

                String[] projection = { MediaStore.Images.Media.DATA };
                @SuppressWarnings("deprecation")
                Cursor cursor = managedQuery(selectedImage, projection, null, null, null);
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                mCurrentPhotoPath = cursor.getString(column_index);

                Log.e("DebugGalleryImage:",mCurrentPhotoPath);

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
}
