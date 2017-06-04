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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import it.polito.mad.team19.mad_expenses.Classes.FirebaseExpense;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by Jured on 26/05/17.
 */

public class ExpenseHistoryAdapter extends BaseAdapter {

    ArrayList<FirebaseExpense> historyList;
    Activity context;

    public ExpenseHistoryAdapter(Context historyPopupActivity, ArrayList<FirebaseExpense> expensesHistory) {
        this.historyList = expensesHistory;
        this.context = (Activity) historyPopupActivity;
    }

    @Override
    public int getCount() {
        return historyList.size();
    }

    @Override
    public Object getItem(int position) {
        return historyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.d("DebugHistory", "debug elemento vuoto" + position + " " + historyList.size());

        final ExpenseHistoryAdapter.ImgHolder viewHolder;

        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.expense_history_popup_list_row, parent, false);
            viewHolder = new ExpenseHistoryAdapter.ImgHolder();
            viewHolder.img = (ImageView) convertView.findViewById(R.id.expense_history_list_iv);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ExpenseHistoryAdapter.ImgHolder) convertView.getTag();

        TextView modifyData = (TextView) convertView.findViewById(R.id.history_item_tv);
        TextView modifyTime = (TextView) convertView.findViewById(R.id.history_modify_time_tv);

        if (historyList.get(position).getImage() != null)
            Glide.with(context).load(historyList.get(position).getImage()).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_user_noimg).centerCrop().error(R.drawable.ic_user_noimg).into(new BitmapImageViewTarget(viewHolder.img) {
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

        Log.d("DebugHistory", "modifyTime: "+ historyList.get(position).getModTime());
        modifyData.setText(historyList.get(position).getName());

        Calendar c = Calendar.getInstance();
        SimpleDateFormat format1 = new SimpleDateFormat(context.getResources().getString(R.string.date_time_format));
        c.setTimeInMillis(Long.parseLong(historyList.get(position).getModTime()));
        modifyTime.setText(context.getResources().getString(R.string.modify_on) + c.toString());

        //Jured: se provo a settare la data nel campo principale lo fa tranquillamente
        //modifyData.setText(format1.format(c.getTime()));
        return convertView;
    }

//    public int getPositionFromUid(String Uid) {
//        for (int position = 0; position < historyList.size(); position++) {
//            if (historyList.get(position).getUid().equals(Uid)) {
//                Log.d("GroupMembersAdapter", "getPositionFromUid - Returning no." + position);
//                return position;
//            }
//        }
//        return -1;
//    }

    static class ImgHolder {
        ImageView img;
    }
}
