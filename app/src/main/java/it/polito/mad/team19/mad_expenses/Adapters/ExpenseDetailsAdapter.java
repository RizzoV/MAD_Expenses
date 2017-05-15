package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Currency;

import it.polito.mad.team19.mad_expenses.Classes.ExpenseDetail;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by Valentino on 29/04/2017.
 */

public class ExpenseDetailsAdapter extends BaseAdapter {

    private ArrayList<ExpenseDetail> detailsList = new ArrayList<>();
    private Activity context;

    static class ImgHolder {
        ImageView debtor_img;
        ImageView creditor_img;
    }

    public ExpenseDetailsAdapter(Context context, ArrayList<ExpenseDetail> detailsList) {
        this.detailsList = detailsList;
        this.context = (Activity) context;
    }

    public void setListData(ArrayList<ExpenseDetail> detailsList) {
        this.detailsList = detailsList;
    }

    @Override
    public int getCount() {
        return detailsList.size();
    }

    @Override
    public Object getItem(int position) {
        return detailsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ImgHolder viewHolder;


        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.expense_details_list_row, parent, false);
            viewHolder = new ImgHolder();
            viewHolder.creditor_img=(ImageView)convertView.findViewById(R.id.creditor_icon);
            viewHolder.debtor_img=(ImageView)convertView.findViewById(R.id.debtor_icon);
            convertView.setTag(viewHolder);
        }
        else
            viewHolder = (ImgHolder) convertView.getTag();


        TextView creditorName = (TextView) convertView.findViewById(R.id.creditor_name);
        TextView debtorName = (TextView) convertView.findViewById(R.id.debtor_name);
        TextView amount = (TextView) convertView.findViewById(R.id.debt_amount);


        ExpenseDetail ed = detailsList.get(position);

        creditorName.setText(ed.getCreditor());
        debtorName.setText(ed.getDebtor());
        amount.setText(ed.getAmount() + " " + Currency.getInstance("EUR").getSymbol());


        // Manage creditor icon
        if (ed.getCreditorImage() != null) {
            Glide.with(context).load(ed.getCreditorImage()).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.mipmap.ic_user_noimg).centerCrop().error(R.mipmap.ic_user_noimg).into(new BitmapImageViewTarget(viewHolder.creditor_img) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    viewHolder.creditor_img.setImageDrawable(circularBitmapDrawable);
                }
            });
        } else {
            Glide.clear(viewHolder.creditor_img);
            viewHolder.creditor_img.setImageResource(R.mipmap.ic_user_noimg);
        }

        // Manage debtor icon
        if (ed.getDebtorImage() != null) {
            Glide.with(context).load(ed.getDebtorImage()).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.mipmap.ic_user_noimg).centerCrop().error(R.mipmap.ic_user_noimg).into(new BitmapImageViewTarget(viewHolder.debtor_img) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    viewHolder.debtor_img.setImageDrawable(circularBitmapDrawable);
                }
            });
        } else {
            Glide.clear(viewHolder.debtor_img);
            viewHolder.debtor_img.setImageResource(R.mipmap.ic_user_noimg);
        }

        return convertView;
    }

}
