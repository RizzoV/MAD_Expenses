package it.polito.mad.team19.mad_expenses;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;


public class CreateGroupActivity extends AppCompatActivity {

    CheckBox distributed;
    CheckBox centralized;
    Button add_group;
    EditText group_name;
    Snackbar bar;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    private ImageButton imageButton;
    private static int RESULT_LOAD_IMAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        getSupportActionBar().setTitle("Crea un nuovo gruppo");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Log.e("User",mAuth.getCurrentUser().getUid());

        distributed = (CheckBox) findViewById(R.id.distributed_checkbox);
        centralized = (CheckBox) findViewById(R.id.centralized_checkbox);
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

        addListenerOnImageButton();


    }

    private void addListenerOnImageButton()
    {
        imageButton = (ImageButton) findViewById(R.id.add_image_btn);

        imageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {

                // TO REPLACE WITH THE CODE FOR THE UPLOAD OF THE IMAGE
                //Snackbar.make(view, "Replace with your image", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                //TO LOAD IMAGE FROM GALLERY (error with RESULT_LOAD_IMAGE)
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

    }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);
            Uri outputUri = Uri.fromFile(new File(getCacheDir(), "croppedImage"));

            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data)
            {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                if(picturePath!=null)
                {
                    Uri inputUri = Uri.fromFile(new File(picturePath));
                    Crop.of(inputUri, outputUri).asSquare().start(this);
                }
            }

            if(requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK)
            {
                ImageView imageView = (ImageView) findViewById(R.id.group_img);
                try {
                    imageView.setImageBitmap(getCircleBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), outputUri)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    private Bitmap getCircleBitmap(Bitmap scaleBitmapImage)
    {
        float scale = getResources().getDisplayMetrics().density;
        int targetHeight = (int)(150*scale + 0.5f);
        int targetWidth = (int)(150*scale + 0.5f);

        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                targetHeight,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap,
                new Rect(0, 0, sourceBitmap.getWidth(),
                        sourceBitmap.getHeight()),
                new Rect(0, 0, targetWidth,
                        targetHeight), null);
        return targetBitmap;
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

        setResult(1);
        finish();

    }


}
