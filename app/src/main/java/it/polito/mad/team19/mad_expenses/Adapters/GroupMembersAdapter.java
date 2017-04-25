package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMembers;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by ikkoyeah on 25/04/17.
 */

public class GroupMembersAdapter extends BaseAdapter {
    ArrayList<FirebaseGroupMembers> membersList;
    Activity context;

    public GroupMembersAdapter(Context context, ArrayList<FirebaseGroupMembers> membersList) {
        this.membersList = membersList;
        this.context = (Activity) context;
    }

    @Override
    public int getCount() {
        return membersList.size();
    }

    @Override
    public Object getItem(int position) {
        return membersList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView==null)
        {
            convertView=context.getLayoutInflater().inflate(R.layout.contributors_list_row,parent,false);
        }

        TextView username = (TextView) convertView.findViewById(R.id.username_checkedtv);

        username.setText(membersList.get(position).getName());


        return convertView;
    }
}
