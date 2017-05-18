package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
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
    String myNot;
    boolean newNot = false;

    public NotificationsAdapter(Context context, ArrayList<Notifications> notificationsList,String myNot) {
        this.notitifcationsList = notificationsList;
        this.context = (Activity) context;
        this.myNot = myNot;
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

        LinearLayout ll = (LinearLayout)convertView.findViewById(R.id.not_container);
        TextView text=(TextView)convertView.findViewById(R.id.notification_text);
        TextView date=(TextView)convertView.findViewById(R.id.notification_date);

        Notifications notification=notitifcationsList.get(notitifcationsList.size()-1-position);

        if(notification.getNotKey().equals(myNot))
            newNot=true;

        if(!newNot)
            ll.setBackgroundColor(Color.WHITE);


            Log.d("notification", notification.getData().toString());
        String[] notDate = notification.getData().split("-");
        date.setText(notDate[0] + " " + getStringMonth(Integer.parseInt(notDate[1])));

        if(notification.getActivity().equals(context.getResources().getString(R.string.notififcationAddExpenseActivity)));
            text.setText(notification.getUname()+" "+context.getResources().getString(R.string.notififcationAddExpenseText));

        if(notification.getActivity().equals(context.getResources().getString(R.string.notififcationAddGroupActivity)));
            text.setText(notification.getUname()+" "+context.getResources().getString(R.string.notififcationAddGroupText));



        return convertView;
    }

    private String getStringMonth(int s)
    {
        switch (s)
        {
            case 1:
                return context.getResources().getString(R.string.january);
            case 2:
                return context.getResources().getString(R.string.february);
            case 3:
                return context.getResources().getString(R.string.march);
            case 4:
                return context.getResources().getString(R.string.april);
            case 5:
                return context.getResources().getString(R.string.may);
            case 6:
                return context.getResources().getString(R.string.june);
            case 7:
                return context.getResources().getString(R.string.july);
            case 8:
                return context.getResources().getString(R.string.august);
            case 9:
                return context.getResources().getString(R.string.september);
            case 10:
                return context.getResources().getString(R.string.october);
            case 11:
                return context.getResources().getString(R.string.november);
            case 12:
                return context.getResources().getString(R.string.december);
            default:
                return "NNN";

        }

    }

}
