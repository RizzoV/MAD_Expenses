package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

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
    OnItemLongClickListener mItemLongClickListener;

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
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        FirebaseGroupMember currentObj = members.get(position);
        holder.setData(currentObj, position);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mItemLongClickListener.onItemLongClick(v, position);
                return false;
            }
        });

    }


    @Override
    public int getItemCount() {
        return members.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView name;
        int position;
        FirebaseGroupMember current;
        ImageView thumb;
        TextView isAdmin;


        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.username_checkedtv);
            thumb = (ImageView) itemView.findViewById(R.id.user_img_contacts_list);
            isAdmin = (TextView) itemView.findViewById(R.id.isAdmin_tv);
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

        //Jured: onLongClick
        @Override
        public boolean onLongClick(View v) {
            Log.d("DebugLongClick", "long click!!");
            return false;
        }


        public void setData(FirebaseGroupMember current, int position) {
            this.name.setText(current.getName());
            this.current = current;
            this.position = position;
            if(current.isAdmin() == 1) {
                this.isAdmin.setVisibility(View.VISIBLE);
            }
            if(current.getImgUrl() != null)
            {
                //modo più semplice per caricare immagini e renderle tonde
                Glide.with(context).load(current.getImgUrl()).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_user_noimg).centerCrop().error(R.drawable.ic_user_noimg).into(new BitmapImageViewTarget(thumb) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);

                        circularBitmapDrawable.setCircular(true);
                        thumb.setImageDrawable(circularBitmapDrawable);
                    }
                });
            }
            else
            {
                //se non ho l'immagine Glide non deve più occuparsene
                Glide.clear(thumb);
                thumb.setImageResource(R.drawable.ic_user_noimg);
            }

        }


    }

    //LUDO: aggiunto metodo onItemClickListener
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    //Jured: onLongClick
    public interface OnItemLongClickListener {
        public boolean onItemLongClick(View v, int position);
    }

    public void SetOnItemLongClickListener (final OnItemLongClickListener mItemLongClickListener){
        this.mItemLongClickListener = mItemLongClickListener;
    }


    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
