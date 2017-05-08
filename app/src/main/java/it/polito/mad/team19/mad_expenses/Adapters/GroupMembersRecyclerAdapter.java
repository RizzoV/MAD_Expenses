package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by ikkoyeah on 26/04/17.
 */

public class GroupMembersRecyclerAdapter extends RecyclerView.Adapter<GroupMembersRecyclerAdapter.MyViewHolder> {

    ArrayList<FirebaseGroupMember> members;
    Activity context;
    private LayoutInflater mInflater;
    //LUDO: aggiunto metodo onItemClickListener
    OnItemClickListener mItemClickListener;

    public GroupMembersRecyclerAdapter(Context context, ArrayList<FirebaseGroupMember> expenses) {
        this.members = expenses;
        this.context = (Activity) context;
        this.mInflater = LayoutInflater.from(this.context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.group_members_popup_list_row, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        FirebaseGroupMember currentObj = members.get(position);
        holder.setData(currentObj, position);
    }


    @Override
    public int getItemCount() {
        return members.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name;
        int position;
        FirebaseGroupMember current;


        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.username_checkedtv);
            //LUDO: aggiunto metodo onItemClickListener
            itemView.setOnClickListener(this);
        }

        //LUDO: aggiunto metodo onItemClickListener

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, position);
            }
        }

        public void setData(FirebaseGroupMember current, int position) {
            this.name.setText(current.getName());
            this.current = current;
            this.position = position;
        }

    }
    //LUDO: aggiunto metodo onItemClickListener


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
