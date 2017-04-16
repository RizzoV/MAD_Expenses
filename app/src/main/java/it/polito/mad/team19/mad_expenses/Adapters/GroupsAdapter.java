package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Classes.Group;
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
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView==null)
        {
            convertView=context.getLayoutInflater().inflate(R.layout.groups_list_row,parent,false);
        }

        TextView name=(TextView)convertView.findViewById(R.id.group_name_tv);
        TextView balance=(TextView)convertView.findViewById(R.id.balance_tv);
        TextView notifications=(TextView)convertView.findViewById(R.id.notification_cnt_tv);
        ImageView image = (ImageView) convertView.findViewById(R.id.group_image);
        ImageView notification_back = (ImageView) convertView.findViewById(R.id.notification_back);


        Group group=groupList.get(position);

        name.setText(group.getName());

        Float balanceAmount = group.getBalance();
        if(balanceAmount>0)
            balance.setText("Devi dare: " + String.format("%.2f", group.getBalance()));
        if(balanceAmount<0)
            balance.setText("Devi ricevere: " + String.format("%.2f", group.getBalance()));

        if(balanceAmount==0)
            balance.setText("Non hai nessun debito/credito");

        if(group.getNotifyCnt()>0)
            notifications.setText(group.getNotifyCnt().toString());
        else {
            notifications.setVisibility(View.INVISIBLE);
            notification_back.setVisibility(View.INVISIBLE);
        }

        image.setImageResource(R.drawable.circle);

        return convertView;
    }
}
