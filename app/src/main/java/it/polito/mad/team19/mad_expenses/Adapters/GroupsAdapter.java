package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Group;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by Jured on 24/03/17.
 */

public class GroupsAdapter extends BaseAdapter {

    ArrayList<Group> groupList;
    Activity context;


    public GroupsAdapter(Context context, ArrayList<Group> groupList) {
        this.groupList = groupList;
        this.context = (Activity) context;
    }

    @Override
    public int getCount() {
        return groupList.size();
    }

    @Override
    public Object getItem(int position) {
        return groupList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null) {
            convertView=context.getLayoutInflater().inflate(R.layout.group_row,parent,false);
        }
        TextView name=(TextView)convertView.findViewById(R.id.group_name_tv);
        TextView credits=(TextView)convertView.findViewById(R.id.credits_amount_tv);
        TextView debits=(TextView)convertView.findViewById(R.id.debits_amount_tv);
        TextView notifications=(TextView)convertView.findViewById(R.id.notification_cnt_tv);
        ImageView image = (ImageView) convertView.findViewById(R.id.group_image);
        Group group=groupList.get(position);
        name.setText(group.getName());
        credits.setText(group.getCredits().toString());
        debits.setText(group.getDebits().toString());
        notifications.setText(group.getNotifyCnt().toString());
        image.setImageResource(R.drawable.icona_a_caso);
        return convertView;
    }
}
