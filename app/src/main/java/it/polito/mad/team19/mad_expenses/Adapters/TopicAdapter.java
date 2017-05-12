package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Classes.Topic;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by ikkoyeah on 12/05/17.
 */

public class TopicAdapter extends BaseAdapter {
    ArrayList<Topic> msgList;
    Activity context;

    public TopicAdapter(Context context, ArrayList<Topic> msgList) {
        this.msgList = msgList;
        this.context = (Activity) context;
    }

    @Override
    public int getCount() {
        return msgList.size();
    }

    @Override
    public Object getItem(int position) {
        return msgList.get(position);
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
            convertView=context.getLayoutInflater().inflate(R.layout.topic_msg_row,parent,false);
        }

        RelativeLayout container = (RelativeLayout) convertView.findViewById(R.id.container);
        TextView name = (TextView) convertView.findViewById(R.id.name_msg);
        TextView msg = (TextView) convertView.findViewById(R.id.msg);


        if(msgList.get(position).isMe())
        {
            name.setVisibility(View.GONE);
            float density = context.getResources().getDisplayMetrics().density;
            int paddingDp = (int)(10 * density);
            msg.setPadding(paddingDp,paddingDp,paddingDp,0);
            container.setBackground(context.getResources().getDrawable(R.drawable.chat_my_back));
        }
        else
        {
            name.setText(msgList.get(position).getName());
            container.setBackground(context.getResources().getDrawable(R.drawable.chat_back));
        }



        msg.setText(msgList.get(position).getMsg());


        return convertView;
    }

}
