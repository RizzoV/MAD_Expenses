package it.polito.mad.team19.mad_expenses;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
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
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularfillableloaders.CircularFillableLoaders;

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

    NetworkChangeReceiver netChange;
    IntentFilter filter;

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

    CircularFillableLoaders imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);


        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netChange = new NetworkChangeReceiver();
        netChange.setViewForSnackbar(findViewById(android.R.id.content));
        netChange.setDialogShowTrue(false);
        registerReceiver(netChange, filter);

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

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String date[] = dateEditText.getText().toString().split("/");
                Bundle argsbundle = new Bundle();

                if (date.length == 3) {
                    argsbundle.putInt("year", Integer.valueOf(date[2]));
                    argsbundle.putInt("month", Integer.valueOf(date[1]) - 1);
                    argsbundle.putInt("day", Integer.valueOf(date[0]));
                } else {
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
        } else {
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

        if (requestCode == REQUEST_CONTRIBUTORS) {
            if (resultCode == RESULT_OK) {
                Bundle b = data.getBundleExtra("contributorsBundle");
                contributorsList = b.getParcelableArrayList("parceledContributors");
                if(contributorsList != null) {
                    for (FirebaseGroupMember m : contributorsList)
                        Log.d("CurrentContributor", m.getName() + " ");
                }
            }
        }

        if (requestCode == REQUEST_EXCLUDED) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                excludedList = extras.getParcelableArrayList("parceledExcluded");
                if(excludedList != null) {
                    for (FirebaseGroupMember m : excludedList)
                        Log.d("CurrentExcluded", m.getName() + " ");
                }
            }
        }
    }

    public void setDataEditText(String date) {
        dateEditText.setText(date);
    }


    private void addListenerOnContributorsButton() {
        final Button contributorsButton = (Button) findViewById(R.id.contributors_button);

        if (mAuth.getCurrentUser().getPhotoUrl() != null)
            contributorsList.add(new FirebaseGroupMember(mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getPhotoUrl().toString(), mAuth.getCurrentUser().getUid()));
        else
            contributorsList.add(new FirebaseGroupMember(mAuth.getCurrentUser().getDisplayName(), null, mAuth.getCurrentUser().getUid()));

        Log.d("Contributors", contributorsList.get(0).getName());

        contributorsButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AddExpenseActivity.this, ContributorsPopupActivity.class);
                i.putExtra("groupId", groupId);
                ArrayList<FirebaseGroupMember> initialContributors = new ArrayList<>(contributorsList);
                // All'inizio chi crea la spesa è un contributor
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

        FloatingActionButton doneFab = (FloatingActionButton) findViewById(R.id.new_expense_done_btn);
        doneFab.setOnClickListener(new View.OnClickListener() {
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
                        imageLoader = (CircularFillableLoaders) barProgressDialog.findViewById(R.id.circularFillableLoaders);
                    }
                };

                barProgressDialog.setCancelable(false);
                barProgressDialog.show();

                boolean invalidFields = false;

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

        AsyncFirebaseExpenseLoader async = new AsyncFirebaseExpenseLoader(idExpense, groupId, usrId, mCurrentPhotoPath, mCurrentPhotoName,
                nameEditText.getText().toString(), descriptionEditText.getText().toString(), costEditText.getText().toString(),
                isModifyActivity, oldExpenseId, excludedList, contributorsList,this);

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

        final Map<String, Notifications> notification = new HashMap<String, Notifications>();

        final String finalUsername = username;
        notificationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot current : dataSnapshot.getChildren()) {
                    Notifications currentNot = current.getValue(Notifications.class);
                    notification.put(current.getKey(), new Notifications(currentNot.getActivity(), currentNot.getData(), currentNot.getId(), currentNot.getUid(), currentNot.getUname(), current.getKey()));
                }

                if(getIntent().getStringExtra("CreateExpenseFromProposal")!=null)
                    notification.put(notificationId, new Notifications(getResources().getString(R.string.notififcationAddExpenseFromProposalActivity), formattedDate, idExpense, usrId, finalUsername));
                else
                    notification.put(notificationId, new Notifications(getResources().getString(R.string.notififcationAddExpenseActivity), formattedDate, idExpense, usrId, finalUsername));

                notificationRef.setValue(notification);

                DatabaseReference myNotRef = FirebaseDatabase.getInstance().getReference().child("utenti").child(usrId).child("gruppi").child(groupId).child("notifiche");
                myNotRef.setValue(notificationId);

                }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AddExpenseActivity", "Unable to perform listen on notificationRef");
            }
        });


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
        if (isModifyActivity && (getIntent().getStringExtra("butDoNotTrack") == null)) {
            moveFirebaseExpenseNode();
        }
        // Vale: e della trasformazione di una proposta in spesa
        else if(getIntent().getStringExtra("butDoNotTrack") != null) {
            FirebaseDatabase.getInstance().getReference().child("gruppi").child(groupId).child("membri").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<FirebaseGroupMember> groupMembersList = new ArrayList<>();
                    if(dataSnapshot.hasChildren()) {
                        for(DataSnapshot groupMember : dataSnapshot.getChildren()) {
                            groupMembersList.add(new FirebaseGroupMember(groupMember.child("nome").getValue(String.class), groupMember.child("immagine").getValue(String.class), groupMember.getKey()));
                        }
                    }

                    AsyncFirebaseBalanceLoader async = new AsyncFirebaseBalanceLoader(groupId, idExpense, groupMembersList, expenseTotal, contributorsList, excludedList);
                    async.execute();
                    barProgressDialog.dismiss();
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("AddExpenseActivity", "Could not retrieve the list of group members");
                }
            });
        }
        else {
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
        Bitmap fileBitmap = shrinkBitmap(mCurrentPhotoPath, 1000, 1000);
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
            month = month + 1;
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


            if (!getIntent().getParcelableArrayListExtra("contributorsList").isEmpty()) {
                contributorsList = getIntent().getParcelableArrayListExtra("contributorsList");
            }

            if (!getIntent().getParcelableArrayListExtra("excludedList").isEmpty()) {
                excludedList = getIntent().getParcelableArrayListExtra("excludedList");
            }

            getSupportActionBar().setTitle(R.string.modify_expense);

            nameEditText.setText(oldName);
            descriptionEditText.setText(oldDesc);
            costEditText.setText(oldCost);

            try {
                Glide.with(this).load(oldImgUrl).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().error(R.drawable.circle).into(new BitmapImageViewTarget(mImageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        mImageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
            } catch (Exception e) {
                Log.e("ExpenseDetailsActivity", "Exception:\n" + e.toString());
            }

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
                        setResult(RESULT_OK, getIntent());
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
        } else return super.getSupportParentActivityIntent();
    }

    @Override
    public Intent getParentActivityIntent() {
        if (getIntent().getStringExtra("ModifyIntent") != null) {
            return getParentActivityIntentImpl();
        } else return super.getParentActivityIntent();
    }

    private Intent getParentActivityIntentImpl() {
        Intent i;

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
// other 'case' lines to check for other
// permissions this app might request


