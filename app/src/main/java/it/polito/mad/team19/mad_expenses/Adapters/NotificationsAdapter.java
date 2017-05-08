package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Classes.Notifications;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by ikkoyeah on 08/05/17.
 */

public class NotificationsAdapter extends BaseAdapter {

    ArrayList<Notifications> notitifcationsList;
    Activity context;

    public NotificationsAdapter(Context context, ArrayList<Notifications> notificationsList) {
        this.notitifcationsList = notificationsList;
        this.context = (Activity) context;
    }

    @Override
    public int getCount() {
        return notitifcationsList.size();
    }

    @Override
    public Object getItem(int position) {
        return notitifcationsList.get(position);
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
            convertView=context.getLayoutInflater().inflate(R.layout.layout_notifications_list_row,parent,false);
        }

        TextView text=(TextView)convertView.findViewById(R.id.notification_text);
        TextView date=(TextView)convertView.findViewById(R.id.notification_date);


        Notifications notification=notitifcationsList.get(position);
        text.setText(notification.getDesc());
        date.setText(notification.getDate());


        /* Manage group name */

        return convertView;
    }

}
