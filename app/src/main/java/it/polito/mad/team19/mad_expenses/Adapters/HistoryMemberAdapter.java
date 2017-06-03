package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
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
import it.polito.mad.team19.mad_expenses.ExpenseDetailsActivity;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by ikkoyeah on 25/04/17.
 */

public class HistoryMemberAdapter extends BaseAdapter {
    ArrayList<FirebaseGroupMember> membersList;
    ArrayList<FirebaseGroupMember> contributorsMembersList;
    ArrayList<FirebaseGroupMember> excludedMembersList;
    ArrayList<FirebaseGroupMember> historyDebtorMemberList;
    Activity context;
    boolean isExcludedLayout;

    public HistoryMemberAdapter(Context expenseDetailsActivity, ArrayList<FirebaseGroupMember> historyContributorsMemberList, ArrayList<FirebaseGroupMember> historyExcludedMemberList,ArrayList<FirebaseGroupMember> historyDebtorMemberList) {
        this.contributorsMembersList = historyContributorsMemberList;
        this.excludedMembersList = historyExcludedMemberList;
        this.historyDebtorMemberList = historyDebtorMemberList;
        this.membersList = new ArrayList<>(historyContributorsMemberList);
        this.membersList.addAll(historyExcludedMemberList);
        this.membersList.addAll(historyDebtorMemberList);
        this.context = (Activity) expenseDetailsActivity;
    }


    static class ImgHolder {
        ImageView img;
    }

    public HistoryMemberAdapter(Context context, ArrayList<FirebaseGroupMember> membersList) {
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

        final ImgHolder viewHolder;

        if (convertView == null) {

            convertView = context.getLayoutInflater().inflate(R.layout.history_member_list_row, parent, false);

            viewHolder = new ImgHolder();
            viewHolder.img = (ImageView) convertView.findViewById(R.id.user_img_history_list);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ImgHolder) convertView.getTag();

        TextView username = (TextView) convertView.findViewById(R.id.username_history_tv);

        //Jured: per segnare contributors
        ImageView cb = (ImageView) convertView.findViewById(R.id.contributor_history_iv);
        //Jured: per segnare esclusi
        ImageView ex = (ImageView) convertView.findViewById(R.id.excluded_history_iv);

        if (membersList.get(position).getImgUrl() != null) {
            Glide.with(context).load(membersList.get(position).getImgUrl()).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_user_noimg).centerCrop().error(R.drawable.ic_user_noimg).into(new BitmapImageViewTarget(viewHolder.img) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    viewHolder.img.setImageDrawable(circularBitmapDrawable);
                }
            });
        }
        else {
            Glide.clear(viewHolder.img);
            viewHolder.img.setImageResource(R.drawable.ic_user_noimg);
        }

        username.setText(membersList.get(position).getName());

        if (position < contributorsMembersList.size()) {
            Log.d("DebugHistory", "contribution " + position + " " + contributorsMembersList.size() + membersList.get(position).getName());
            cb.setVisibility(View.VISIBLE);
        }
        else if (position < contributorsMembersList.size() + excludedMembersList.size()) {
            Log.d("DebugHistory", "stikethrough " + position + " " + membersList.size() + membersList.get(position).getName());
            ex.setVisibility(View.VISIBLE);
            convertView.setAlpha(new Float(0.5));
        }

        return convertView;
    }

    public int getPositionFromUid(String Uid) {
        for (int position = 0; position < membersList.size(); position++) {
            if (membersList.get(position).getUid().equals(Uid)) {
                Log.d("HistoryMemberAdapter", "getPositionFromUid - Returning no." + position);
                return position;
            }
        }
        return -1;
    }
}