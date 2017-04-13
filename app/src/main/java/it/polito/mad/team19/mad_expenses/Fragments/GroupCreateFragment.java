package it.polito.mad.team19.mad_expenses.Fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Classes.UserContact;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by ikkoyeah on 14/04/17.
 */


public class GroupCreateFragment extends Fragment {

    /** Variables */

    public GroupCreateFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public GroupCreateFragment newInstance(int sectionNumber)
    {
        GroupCreateFragment fragment = new GroupCreateFragment();


        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.activity_create_group_frag2, container, false);
        ArrayList<UserContact> contacts = getArguments().getParcelableArrayList("contacts");

        TextView tw = (TextView) rootView.findViewById(R.id.textview);
        tw.setText("Contatti selezionati: \n"+contacts.toString());


        return rootView;
    }
}

