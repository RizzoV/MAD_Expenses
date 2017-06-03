package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
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

import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;
import it.polito.mad.team19.mad_expenses.ExcludedPopupActivity;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by ikkoyeah on 25/04/17.
 */

public class GroupMembersAdapter extends BaseAdapter {
    ArrayList<FirebaseGroupMember> membersList;
    Activity context;
    boolean isExcludedLayout;

    static class ImgHolder {
        ImageView img;
    }

    public GroupMembersAdapter(Context context, ArrayList<FirebaseGroupMember> membersList, boolean isExcludedLayout) {
        this.membersList = membersList;
        this.context = (Activity) context;
        this.isExcludedLayout = isExcludedLayout;
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

        final ImgHolder viewHolder;

        if (convertView == null) {
            if(isExcludedLayout)
                convertView = context.getLayoutInflater().inflate(R.layout.group_members_excluded, parent, false);
            else
                convertView = context.getLayoutInflater().inflate(R.layout.group_members_popup_list_row, parent, false);

            viewHolder = new ImgHolder();
            viewHolder.img = (ImageView) convertView.findViewById(R.id.user_img_contacts_list);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ImgHolder) convertView.getTag();

        TextView username = (TextView) convertView.findViewById(R.id.username_checkedtv);

        //LUDO: per ceccare
        ImageView cb = (ImageView) convertView.findViewById(R.id.contributor_checkbox);

        if (membersList.get(position).getImgUrl() != null)
            Glide.with(context).load(membersList.get(position).getImgUrl()).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_user_noimg).centerCrop().error(R.drawable.ic_user_noimg).into(new BitmapImageViewTarget(viewHolder.img) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    viewHolder.img.setImageDrawable(circularBitmapDrawable);
                }
            });
        else {
            Glide.clear(viewHolder.img);
            viewHolder.img.setImageResource(R.drawable.ic_user_noimg);
        }


        if (membersList.get(position).isChecked()) {
            cb.setVisibility(View.VISIBLE);
            if (context instanceof ExcludedPopupActivity) {
                convertView.setAlpha(0.5f);
            }
        }
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