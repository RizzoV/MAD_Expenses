package it.polito.mad.team19.mad_expenses;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import it.polito.mad.team19.mad_expenses.Fragments.UserListFragment;

public class CreateGroupActivity extends AppCompatActivity {

     MenuItem btn_get_contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.create_group_fragment_frame, new UserListFragment()).commit();

        getSupportActionBar().setHomeButtonEnabled(true);
        checkPermissions(10);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_addgroup, menu);
        btn_get_contacts = menu.findItem(R.id.getContacts);
        btn_get_contacts.setEnabled(false);
        btn_get_contacts.setVisible(false);
        return true;
    }


    private boolean checkPermissions(int returnCode) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(CreateGroupActivity.this, new String[]{Manifest.permission.READ_CONTACTS}
                        , returnCode);
            }
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 10: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                else {
                    return;
                }
            }
        }
    }


}
