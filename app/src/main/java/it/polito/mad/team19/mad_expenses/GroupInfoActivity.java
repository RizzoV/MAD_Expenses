package it.polito.mad.team19.mad_expenses;

import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class GroupInfoActivity extends AppCompatActivity {

    ImageView image;
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        image = (ImageView) findViewById(R.id.group_info_toolbar_image_iv);
        toolbar = (Toolbar) findViewById(R.id.group_info_tb);

        String imageUrl = getIntent().getStringExtra("groupImage");
        String groupName = getIntent().getStringExtra("groupName");
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle(groupName.toString());
        Log.e("DebugGroupInfo",groupName);


        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.group_info_ctb);
        collapsingToolbar.setTitle(groupName.toString());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

}
