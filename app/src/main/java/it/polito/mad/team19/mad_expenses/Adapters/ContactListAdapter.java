package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Classes.Expense;
import it.polito.mad.team19.mad_expenses.Classes.UserContact;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by ikkoyeah on 11/04/17.
 */

public class ContactListAdapter extends BaseAdapter{
    private ArrayList<UserContact> contacts;
    public ArrayList<UserContact> filterList;
    Activity context;

    public ContactListAdapter(Context context, ArrayList<UserContact> contactsList)
    {
        filterList = contactsList;
        this.context = (Activity) context;
        this.contacts = new ArrayList<UserContact>();
        this.contacts.addAll(filterList);
    }

    @Override
    public int getCount() {
        return filterList.size();
    }


    @Override
    public Object getItem(int position) {
        return filterList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView==null)
        {
            convertView=context.getLayoutInflater().inflate(R.layout.fragment_contactslist,parent,false);
        }

        LinearLayout ll = (LinearLayout) convertView.findViewById(R.id.layout);
        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView email = (TextView) convertView.findViewById(R.id.email);
        ImageView thumb = (ImageView) convertView.findViewById(R.id.thumb);
        ImageView checked_iv = (ImageView) convertView.findViewById(R.id.checked);

        UserContact contact = filterList.get(position);

        if(contact.isChecked())
            checked_iv.setVisibility(View.VISIBLE);
        else
            checked_iv.setVisibility(View.INVISIBLE);

        if(contact.isGone()) {
            ll.setLayoutParams(new AbsListView.LayoutParams(-1,1));
            ll.setVisibility(View.GONE);
        }
        else {
            ll.setLayoutParams(new AbsListView.LayoutParams(-1,-2));
            ll.setVisibility(View.VISIBLE);
        }


        contact.setPosition(position);

        name.setText(contact.getName());
        email.setText(contact.getEmail());

        try {

            if (contact.getThumb() != null) {
                thumb.setImageBitmap(contact.getThumb());
            } else {
                thumb.setImageResource(R.drawable.circle);
            }
            // Seting round image
        } catch (OutOfMemoryError e) {
            // Add default picture
            e.printStackTrace();
        }



        return convertView;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase();
        filterList.clear();
        if (charText.isEmpty()) {
            filterList.addAll(contacts);
        } else {
            for (int i=0;i<contacts.size();i++) {
                UserContact wp = contacts.get(i);
                if (wp.getName().toLowerCase().contains(charText)) {
                    filterList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }


}