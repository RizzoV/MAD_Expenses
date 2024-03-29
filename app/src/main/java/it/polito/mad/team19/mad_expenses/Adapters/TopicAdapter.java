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

import static android.widget.RelativeLayout.ALIGN_PARENT_LEFT;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;

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

        if (convertView==null) {
            convertView=context.getLayoutInflater().inflate(R.layout.topic_msg_row,parent,false);
        }

        RelativeLayout container = (RelativeLayout) convertView.findViewById(R.id.container);
        TextView name = (TextView) convertView.findViewById(R.id.name_msg);
        TextView msg = (TextView) convertView.findViewById(R.id.msg);
        TextView date = (TextView) convertView.findViewById(R.id.date_msg);



        date.setText(msgList.get(position).getDate());

        float density = context.getResources().getDisplayMetrics().density;
        int paddingDp = (int)(5 * density);

        if(msgList.get(position).isMe())
        {
            name.setVisibility(View.GONE);
            msg.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            date.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            msg.setPadding(paddingDp,paddingDp,paddingDp,0);

            container.setBackground(context.getResources().getDrawable(R.drawable.chat_my_bubble));
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(ALIGN_PARENT_LEFT);
            container.setLayoutParams(params);
        }
        else
            {
            name.setVisibility(View.VISIBLE);
            name.setText(msgList.get(position).getName());
            name.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            date.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            msg.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            msg.setPadding(paddingDp,0,paddingDp,0);

            container.setBackground(context.getResources().getDrawable(R.drawable.chat_other_bubble));
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(ALIGN_PARENT_RIGHT);
            container.setLayoutParams(params);

        }



        msg.setText(msgList.get(position).getMsg());


        return convertView;
    }

}
