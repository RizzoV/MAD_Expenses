package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by ikkoyeah on 25/04/17.
 */

public class GroupMembersAdapter extends BaseAdapter {
    ArrayList<FirebaseGroupMember> membersList;
    Activity context;

    public GroupMembersAdapter(Context context, ArrayList<FirebaseGroupMember> membersList) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.group_members_popup_list_row, parent, false);
        }
        TextView username = (TextView) convertView.findViewById(R.id.username_checkedtv);

        //LUDO: per ceccare
        ImageView cb = (ImageView) convertView.findViewById(R.id.contributor_checkbox);

        if(membersList.get(position).isChecked())
            cb.setVisibility(View.VISIBLE);
        else
            cb.setVisibility(View.INVISIBLE);

        username.setText(membersList.get(position).getName());
        return convertView;
    }

    public int getPositionFromUid(String Uid) {
        for (int position = 0; position < membersList.size(); position++) {
            if (membersList.get(position).getUid().equals(Uid)) {
                Log.d("GroupMembersAdapter", "getPositionFromUid - Returning no." + position);
                return position;
            }
        }
        return -1;
    }
}