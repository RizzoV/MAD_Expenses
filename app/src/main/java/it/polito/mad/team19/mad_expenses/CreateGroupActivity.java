package it.polito.mad.team19.mad_expenses;

import android.*;
import android.Manifest;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CreateGroupActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.users_list_fragment, new UsersListFragment()).commit();

        getSupportActionBar().setHomeButtonEnabled(true);
        checkPermissions(10);
    }

    public static class UsersListFragment extends Fragment {

        /** Variables */

        public UsersListFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static UsersListFragment newInstance(int sectionNumber)
        {
            UsersListFragment fragment = new UsersListFragment();

            return fragment;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            final View rootView = inflater.inflate(R.layout.activity_create_group_frag1, container, false);
            return rootView;
        }
    }

    public static class GroupSettingFragment extends Fragment {

        /** Variables */

        public GroupSettingFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static GroupSettingFragment newInstance(int sectionNumber)
        {
            GroupSettingFragment fragment = new GroupSettingFragment();

            return fragment;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            final View rootView = inflater.inflate(R.layout.activity_create_group_frag2, container, false);
            return rootView;
        }
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
