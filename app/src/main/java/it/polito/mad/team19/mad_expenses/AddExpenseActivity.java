package it.polito.mad.team19.mad_expenses;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by Bolz on 03/04/2017.
 */

public class AddExpenseActivity extends AppCompatActivity
{
    ImageButton imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        //imageButton = (ImageButton) findViewById(R.id.image);

        addListenerOnButton();

    }

    public void addListenerOnButton()
    {

        imageButton = (ImageButton) findViewById(R.id.image);

        imageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //FOR EXAMPLE
               // Toast.makeText(MyAndroidAppActivity.this,"ImageButton is clicked!", Toast.LENGTH_SHORT).show();

                // TO REPLACE WITH THE CODE FOR THE UPLOAD OF THE IMAGE
                Snackbar.make(view, "Replace with your image", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                //TO LOAD IMAGE FROM GALLERY (error with RESULT_LOAD_IMAGE)
                //Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //startActivityForResult(i, RESULT_LOAD_IMAGE);
            }

        });

        /*@Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            // String picturePath contains the path of selected Image
        }*/

    }


}
