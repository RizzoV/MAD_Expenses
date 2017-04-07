package it.polito.mad.team19.mad_expenses;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Bolz on 07/04/2017.
 */

public class AuthActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

    }
}
