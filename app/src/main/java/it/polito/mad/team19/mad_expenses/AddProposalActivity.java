package it.polito.mad.team19.mad_expenses;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;

/**
 * Created by Valentino on 04/04/2017.
 */

public class AddProposalActivity extends AppCompatActivity {

    ImageButton imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_proposal);

        setTitle(R.string.create_new_proposal);

        //imageButton = (ImageButton) findViewById(R.id.image);

        addListenerOnButton();

    }

    public void addListenerOnButton() {
    }



}
