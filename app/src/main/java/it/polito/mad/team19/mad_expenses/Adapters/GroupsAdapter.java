package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Classes.Group;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by Jured on 24/03/17.
 */



public class GroupsAdapter extends BaseAdapter {

    ArrayList<Group> groupList;
    Activity context;


    static class ImgHolder {
        TextView name;
        TextView balance;
        TextView notifications;
        ImageView notification_back;
        ImageView image;
    }

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

        final ImgHolder viewHolder;
        if (convertView==null)
        {
            convertView=context.getLayoutInflater().inflate(R.layout.groups_list_row,parent,false);
            viewHolder = new ImgHolder();
            viewHolder.name=(TextView)convertView.findViewById(R.id.group_name_tv);
            viewHolder.balance=(TextView)convertView.findViewById(R.id.balance_tv);
            viewHolder.notifications=(TextView)convertView.findViewById(R.id.notification_cnt_tv);
            viewHolder.notification_back = (ImageView) convertView.findViewById(R.id.notification_back);
            viewHolder.image = (ImageView) convertView.findViewById(R.id.group_image);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ImgHolder) convertView.getTag();
        }



        final Group group=groupList.get(position);

        /* Manage group name */
        viewHolder.name.setText(group.getName());

        /* Manage personal balance in group */
        Float balanceAmount = group.getBalance();
        if(balanceAmount>0) {
            viewHolder.balance.setText("Devi ricevere: " + String.format("%.2f", group.getBalance()));
            viewHolder.balance.setTextColor(ContextCompat.getColor(context, R.color.textGreen));

        }
        if(balanceAmount<0)
        {
            viewHolder.balance.setText("Devi dare: " + String.format("%.2f", -group.getBalance()));
            viewHolder.balance.setTextColor(ContextCompat.getColor(context, R.color.redMaterial));

        }
        if(balanceAmount==0)
            viewHolder.balance.setText("Non hai nessun debito/credito");

        /* Manage notifications count */

        Log.d("adapter",group.getGroupId()+" "+group.getNotifyCnt()+"");

        if(group.getNotifyCnt()>0) {
            viewHolder.notifications.setVisibility(View.VISIBLE);
            viewHolder.notification_back.setVisibility(View.VISIBLE);
            viewHolder.notifications.setText(group.getNotifyCnt().toString());
        }
        else {
            viewHolder.notifications.setVisibility(View.INVISIBLE);
            viewHolder.notification_back.setVisibility(View.INVISIBLE);
        }

        /* Manage group image */
        //TODO: prendere l'immagine dalla memoria e non direttamente da firebase (LUDO)
        if(group.getImage() != null)
        {
            Log.d("GroupImageNotNull",group.getName());
            //modo più semplice per caricare immagini e renderle tonde
            Glide.with(context).load(group.getImage()).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.mipmap.ic_group).centerCrop().error(R.mipmap.ic_group).into(new BitmapImageViewTarget(viewHolder.image) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);

                    Log.d("GroupImageNotNull",group.getName());
                    circularBitmapDrawable.setCircular(true);
                    viewHolder.image.setImageDrawable(circularBitmapDrawable);
                }
            });
        }
        else
        {
            //se non ho l'immagine Glide non deve più occuparsene
            Glide.clear(viewHolder.image);
            Log.d("GroupImageNull",group.getName());
            viewHolder.image.setImageResource(R.mipmap.ic_group);
        }

        return convertView;
    }

}
