package it.polito.mad.team19.mad_expenses;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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

import it.polito.mad.team19.mad_expenses.Classes.FirebaseExpense;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;

/**
 * Created by Bolz on 03/04/2017.
 */


public class AddExpenseActivity extends AppCompatActivity implements GalleryOrCameraDialog.NoticeDialogListener {

    private static final int STORAGE_REQUEST = 666;
    private ImageButton imageButton;
    private ImageView mImageView;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseStorage storage;
    static final String COST_REGEX = "[0-9]+[.,]{0,1}[0-9]{0,2}";
    private String groupId;
    private String usrId;
    Boolean isContributorsClicked = true;
    Boolean isExcludedClicked = true;
    private static final int EXP_CREATED = 1;
    private String mCurrentPhotoPath = null;
    private String mCurrentPhotoName;
    private Uri mCurrentPhotoFirebaseUri;
    StorageReference storageRef;
    StorageReference groupImagesRef;
    EditText nameEditText;
    EditText descriptionEditText;
    EditText costEditText;
    public EditText dateEditText;
    float expenseTotal;
    String idExpense;
    ProgressDialog barProgressDialog;
    private ArrayList<FirebaseGroupMember> contributorsList = new ArrayList<>();
    private ArrayList<FirebaseGroupMember> excludedList = new ArrayList<>();

    //Jured: modifyActivity variables
    private Boolean isModifyActivity;
    String oldName;
    String oldDesc;
    String oldImgUrl;
    String oldAuthorId;
    String oldCost;
    String oldGroupId;
    String oldExpenseId;
    String oldExpenseVersionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        mAuth = FirebaseAuth.getInstance();

        isModifyActivity = false;

        groupId = getIntent().getStringExtra("groupId");
        usrId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setTitle(R.string.create_new_expense);

        mImageView = (ImageView) findViewById(R.id.camera_img);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();


        dateEditText = (EditText) findViewById(R.id.new_expense_data_et);
        dateEditText.setInputType(InputType.TYPE_NULL);
        dateEditText.setFocusable(false);

        dateEditText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String date[] = dateEditText.getText().toString().split("/");
                Bundle argsbundle = new Bundle();

                if(date.length==3)
                {
                    argsbundle.putInt("year",Integer.valueOf(date[2]));
                    argsbundle.putInt("month",Integer.valueOf(date[1])-1);
                    argsbundle.putInt("day",Integer.valueOf(date[0]));
                }
                else{
                    Calendar c = Calendar.getInstance();
                    argsbundle.putInt("year", c.get(Calendar.YEAR));
                    argsbundle.putInt("month", c.get(Calendar.MONTH));
                    argsbundle.putInt("day", c.get(Calendar.DAY_OF_MONTH));
                }

                DialogFragment newFragment = new DatePickerFragment();
                newFragment.setArguments(argsbundle);
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        nameEditText = (EditText) findViewById(R.id.new_expense_name_et);
        descriptionEditText = (EditText) findViewById(R.id.new_expense_description_et);
        costEditText = (EditText) findViewById(R.id.new_expense_cost_et);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddExpenseActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_REQUEST);
            } else {
                // The permission is granted, we can perform the action
                addListenerOnDoneButton();
                addListenerOnImageButton();

                // only done for the expenses
                addListenerOnContributorsButton();
                addListenerOnExcludedButton();
                checkCallToModify();
            }
        }
        else
        {
            addListenerOnDoneButton();
            addListenerOnImageButton();

            // only done for the expenses
            addListenerOnContributorsButton();
            addListenerOnExcludedButton();
            checkCallToModify();
        }
    }

    //Jured: aggiunto codice che scatta una foto, la salva su file e poi la carica
    //su firebase in modo totalmente ignorante, sempre alla stessa locazione e per ora senza compressione;

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_GALLERY_IMAGE = 2;
    static final int REQUEST_CONTRIBUTORS = 3;
    static final int REQUEST_EXCLUDED = 4;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        StorageReference storageRef = storage.getReference();
        StorageReference groupImagesRef = storageRef.child("images").child(groupId);

        if (requestCode == REQUEST_TAKE_PHOTO)
            Log.d("DEBUG APP: ", mCurrentPhotoPath);


        if (requestCode == REQUEST_GALLERY_IMAGE) {
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
                setImageView(mCurrentPhotoPath);
            }
        }

        if (requestCode == REQUEST_CONTRIBUTORS) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                contributorsList = extras.getParcelableArrayList("parceledContributors");
                for (FirebaseGroupMember m : contributorsList)
                    Log.d("CurrentContributor", m.getName());
            }
        }

        if (requestCode == REQUEST_EXCLUDED) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                excludedList = extras.getParcelableArrayList("parceledExcluded");
                for (FirebaseGroupMember m : excludedList)
                    Log.d("CurrentExcluded", m.getName());
            }
        }
    }

    public void setDataEditText(String date)
    {
        dateEditText.setText(date);
    }


    private void addListenerOnContributorsButton() {
        final Button contributorsButton = (Button) findViewById(R.id.contributors_button);

        contributorsList.add(new FirebaseGroupMember(mAuth.getCurrentUser().getDisplayName(),null,mAuth.getCurrentUser().getUid()));
        Log.d("Contributors",contributorsList.get(0).getName().toString());


        contributorsButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AddExpenseActivity.this, ContributorsPopupActivity.class);
                i.putExtra("groupId", groupId);
                ArrayList<FirebaseGroupMember> initialContributors = new ArrayList<>(contributorsList);
                //all'inizio chi crea la spesa è un contributor
                Bundle b = new Bundle();
                b.putParcelableArrayList("contributorsList", initialContributors);
                i.putExtra("contributorsBundle", b);
                startActivityForResult(i, REQUEST_CONTRIBUTORS);
            }
        });
    }

    private void addListenerOnExcludedButton() {
        Button excludedButton = (Button) findViewById(R.id.excluded_button);

        excludedButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AddExpenseActivity.this, ExcludedPopupActivity.class);
                i.putExtra("groupId", groupId);
                Bundle b = new Bundle();
                b.putParcelableArrayList("excludedList", excludedList);
                i.putExtra("excludedBundle", b);
                startActivityForResult(i, REQUEST_EXCLUDED);
            }
        });
    }

    private void addListenerOnDoneButton() {

        final String TAG = "firebaseAuth";

        FloatingActionButton doneBtn = (FloatingActionButton) findViewById(R.id.new_expense_done_btn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //LUDO: progressDialog a tutto schermo con sfondo sfocato
                barProgressDialog = new ProgressDialog(AddExpenseActivity.this, R.style.full_screen_dialog) {
                    @Override
                    protected void onCreate(Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);
                        setContentView(R.layout.progress_dialog_layout);
                        getWindow().setLayout(AppBarLayout.LayoutParams.MATCH_PARENT,
                                AppBarLayout.LayoutParams.MATCH_PARENT);
                    }
                };

                barProgressDialog.setCancelable(false);
                barProgressDialog.show();

                boolean invalidFields = false;

                //Jured: spostate nella onCreate();
                /*nameEditText = (EditText) findViewById(R.id.new_expense_name_et);
                descriptionEditText = (EditText) findViewById(R.id.new_expense_description_et);
                costEditText = (EditText) findViewById(R.id.new_expense_cost_et);*/

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
                        }
                    };

                    //TODO tagliare il valore di cost a due cifre decimali
                    expenseTotal = Float.parseFloat(costEditText.getText().toString().replace(",", "."));

                    uploadInfos();
                } else {
                    // In modo da poter riscrivere qualcosa nel campo
                    barProgressDialog.dismiss();
                }
            }
        });
    }

    private void uploadInfos() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("gruppi").child(groupId).child("expenses");
        String uuid = myRef.push().getKey();
        idExpense = uuid;
        final DatabaseReference newExpenseRef = myRef.child(uuid);

        if (mCurrentPhotoPath != null)
        {
            groupImagesRef = storageRef.child("images").child(groupId);
            File imageToUpload = new File(mCurrentPhotoPath);
            Bitmap fileBitmap = ShrinkBitmap(mCurrentPhotoPath, 1000, 1000);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            fileBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            byte[] datas = baos.toByteArray();
            mImageView.setImageBitmap(fileBitmap);
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
                    groupImagesRef.child(mCurrentPhotoName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            //Log.e("DebugIsModifyFlag", oldExpenseVersionId);
                            newExpenseRef.setValue(new FirebaseExpense(usrId, nameEditText.getText().toString(), descriptionEditText.getText().toString(),
                                    Float.valueOf(costEditText.getText().toString().replace(",", ".")), uri.toString()));

                            for (FirebaseGroupMember member : excludedList) {
                                newExpenseRef.child("excluded").child(member.getUid()).setValue(member.getName());
                            }
                            for (FirebaseGroupMember member : contributorsList) {
                                newExpenseRef.child("contributors").child(member.getUid()).setValue(member.getName());
                            }

                            Log.d("DebugIsModifyFlag", isModifyActivity.toString());
                            if (isModifyActivity) {
                                newExpenseRef.child("oldVersionId").setValue(oldExpenseId);
                            }

                            //TODO: aggiungere quello in fondo
                            finishTasks();

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
        else
        {
            Log.d("DebugCaricamentoSpesa", "NoImage");
            newExpenseRef.setValue(new FirebaseExpense(usrId, nameEditText.getText().toString(), descriptionEditText.getText().toString(),
                    Float.valueOf(costEditText.getText().toString().replace(",", "."))));
            for (FirebaseGroupMember member : excludedList) {
                newExpenseRef.child("excluded").child(member.getUid()).setValue(member.getName());
            }
            for (FirebaseGroupMember member : contributorsList) {
                newExpenseRef.child("contributors").child(member.getUid()).setValue(member.getName());
            }

            Log.d("DebugIsModifyFlag", isModifyActivity.toString());
            if (isModifyActivity) {
                newExpenseRef.child("oldVersionId").setValue(oldExpenseId);
            }
            finishTasks();
        }
    }

    public void finishTasks()
    {
        getIntent().putExtra("expenseId", idExpense);
        getIntent().putExtra("expenseTotal", expenseTotal + "");
        getIntent().putExtra("expenseUId", mAuth.getCurrentUser().getUid());
        getIntent().putExtra("expenseUserName", mAuth.getCurrentUser().getDisplayName());
        Bundle b = new Bundle();
        b.putParcelableArrayList("contributors", contributorsList);
        b.putParcelableArrayList("excluded", excludedList);
        getIntent().putExtras(b);
        setResult(RESULT_OK, getIntent());

        //Jured: gestione della modifica
        if (isModifyActivity) {
            moveFirebaseExpenseNode();
        }
        else
        {
            barProgressDialog.dismiss();
            finish();
        }
    }

    public void addListenerOnImageButton() {

        imageButton = (ImageButton) findViewById(R.id.image);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    private void setImageView(String mCurrentPhotoPath) {
        Bitmap fileBitmap = ShrinkBitmap(mCurrentPhotoPath, 800, 800);
        mImageView.setImageBitmap(fileBitmap);
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



    //Jured: gestione opzioni dialog
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

    @Override
    public void onDialogGalleryClick(DialogFragment dialog) {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    addListenerOnDoneButton();
                    addListenerOnImageButton();

                    // only done for the expenses
                    addListenerOnContributorsButton();
                    addListenerOnExcludedButton();

                } else {
                    ActivityCompat.requestPermissions(AddExpenseActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            STORAGE_REQUEST);
                }
        }

        // other 'case' lines to check for other
        // permissions this app might request
    }
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        Calendar c;
        int year = 0, month = 0, day = 0;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker

            if (getArguments() != null) {
                c = Calendar.getInstance();
                year = getArguments().getInt("year");
                month = getArguments().getInt("month");
                day = getArguments().getInt("day");
                c.set(year, month, day);
            }/* else { // If the DueDate EditText line is empty (no previously selected date by the user then
                    // set today's date into the DatePicker.
                    // Calendar class obtains the current date on the device and has fields for
                    // each of the parts of the date: day, month and year.
                    c = Calendar.getInstance();
                    year = c.get(Calendar.YEAR);
                    month = c.get(Calendar.MONTH);
                    day = c.get(Calendar.DAY_OF_MONTH);
                }*/

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            month=month+1;
            ((AddExpenseActivity) getActivity()).setDataEditText(day + "/" + month + "/" + year);
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            //String formattedDate = sdf.format(c.getTime());
        }
    }


    //Jured: setta l'activity se vede che è stata chimata per modificare la spesa
    private void checkCallToModify() {
        Log.d("DebugModifyExpense", "CallToModifyCheck");
        if (getIntent().getStringExtra("ModifyIntent") != null) {
            Log.d("DebugModifyExpense", "CallToModifyDetected");
            oldName = getIntent().getStringExtra("ExpenseName");
            oldDesc = getIntent().getStringExtra("ExpenseDesc");
            oldImgUrl = getIntent().getStringExtra("ExpenseImgUrl");
            oldAuthorId = getIntent().getStringExtra("ExpenseAuthorId");
            oldCost = getIntent().getStringExtra("ExpenseCost");
            oldGroupId = getIntent().getStringExtra("groupId");
            oldExpenseId = getIntent().getStringExtra("ExpenseId");


            if (!getIntent().getBundleExtra("contributorsBundle").getParcelableArrayList("contributorsList").isEmpty())
            {
                contributorsList = getIntent().getBundleExtra("contributorsBundle").getParcelableArrayList("contributorsList");
                Log.d("ContributorB",contributorsList.get(0).getUid().toString());
            }

            if (!getIntent().getBundleExtra("excludedBundle").getParcelableArrayList("excludedList").isEmpty())
            {
                excludedList = getIntent().getBundleExtra("excludedBundle").getParcelableArrayList("excludedList");
            }

            getSupportActionBar().setTitle(R.string.modify_expense);
            //getSupportActionBar().home

            nameEditText.setText(oldName);
            descriptionEditText.setText(oldDesc);
            costEditText.setText(oldCost);

            //TODO checkare i contributors ed excluded della spesa che sto modificando

            isModifyActivity = true;
        }
    }

    private void moveFirebaseExpenseNode() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("gruppi").child(groupId).child("expenses");
        final DatabaseReference oldExpenseRef = myRef.child(oldExpenseId);
        final DatabaseReference newExpenseHistoryRef = database.getReference("storico")
                .child(groupId).child("spese").child(oldExpenseId);

        oldExpenseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                newExpenseHistoryRef.setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener() {

                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.d("moveNode() Failed", databaseError.toString());
                        } else {
                            Log.d("moveNode()", "Success");
                            //delete old node
                            oldExpenseRef.removeValue();
                            oldExpenseVersionId = oldExpenseId;
                        }
                        barProgressDialog.dismiss();
                        finish();
                    }


                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AddExpenseActivity", "Could not retrieve the expense from Firebase");
            }


        });
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        if (getIntent().getStringExtra("ModifyIntent") != null) {
            return getParentActivityIntentImpl();
        }
        else return super.getSupportParentActivityIntent();
    }

    @Override
    public Intent getParentActivityIntent() {
        if (getIntent().getStringExtra("ModifyIntent") != null) {
            return getParentActivityIntentImpl();
        }
        else return super.getParentActivityIntent();
    }

    private Intent getParentActivityIntentImpl() {
        Intent i = null;

        // Here you need to do some logic to determine from which Activity you came.
        // example: you could pass a variable through your Intent extras and check that.
            i = new Intent(this, ExpenseDetailsActivity.class);
            // set any flags or extras that you need.
            // If you are reusing the previous Activity (i.e. bringing it to the top
            // without re-creating a new instance) set these flags:
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            // if you are re-using the parent Activity you may not need to set any extras
            //i.putExtra("someExtra", "whateverYouNeed");
        return i;
    }

    Bitmap ShrinkBitmap(String file, int width, int height){

        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);

        int heightRatio = (int)Math.ceil(bmpFactoryOptions.outHeight/(float)height);
        int widthRatio = (int)Math.ceil(bmpFactoryOptions.outWidth/(float)width);

        if (heightRatio > 1 || widthRatio > 1)
        {
            if (heightRatio > widthRatio)
            {
                bmpFactoryOptions.inSampleSize = heightRatio;
            } else {
                bmpFactoryOptions.inSampleSize = widthRatio;
            }
        }

        bmpFactoryOptions.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
        return bitmap;
    }

}
// other 'case' lines to check for other
// permissions this app might request


