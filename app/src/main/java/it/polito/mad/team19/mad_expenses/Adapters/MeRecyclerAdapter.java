package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import it.polito.mad.team19.mad_expenses.Classes.Me;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by ikkoyeah on 31/03/17.
 */

public class MeRecyclerAdapter extends RecyclerView.Adapter<MeRecyclerAdapter.MyViewHolder> {

    private ArrayList<Me> me;
    private Activity context;
    private LayoutInflater mInflater;
    private OnItemClickListener mItemClickListener;
    private String currencySymbol;
    private Double exchangeRate;

    public MeRecyclerAdapter(Context context, ArrayList<Me> me, String currencySymbol, Double exchangeRate) {
        this.me = me;
        this.context = (Activity) context;
        this.mInflater = LayoutInflater.from(context);
        this.exchangeRate = exchangeRate;
        this.currencySymbol = currencySymbol;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.me_list_fromto_row, parent, false);
        MyViewHolder holder = new MeRecyclerAdapter.MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Me currentObj = me.get(position);
        holder.setData(currentObj, position);
    }

    public Me getItemAtPosition(int position) {
        return me.get(position);
    }


    @Override
    public int getItemCount() {
        return me.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView image;
        TextView name;
        TextView amount;
        int position;
        Me current;

        public MyViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.me_image);
            name = (TextView) itemView.findViewById(R.id.me_fromto);
            amount = (TextView) itemView.findViewById(R.id.me_amount);
            itemView.setOnClickListener(this);

        }

        public void setData(Me current, int position) {
            this.name.setText(current.getName());
            this.amount.setText(String.format(Locale.getDefault(), "%.2f", current.getAmount() * exchangeRate) + " " + currencySymbol);
            if (current.getAmount() > 0)
                amount.setTextColor(ContextCompat.getColor(context, R.color.textGreen));
            else if (current.getAmount() < 0)
                amount.setTextColor(ContextCompat.getColor(context, R.color.redMaterial));
            else
                amount.setTextColor(ContextCompat.getColor(context, R.color.grey));



            if (current.getImgUrl() != null) {
                //modo più semplice per caricare immagini e renderle tonde
                Glide.with(context).load(current.getImgUrl()).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_user_noimg).centerCrop().error(R.drawable.ic_user_noimg).into(new BitmapImageViewTarget(image) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);

                        circularBitmapDrawable.setCircular(true);
                        image.setImageDrawable(circularBitmapDrawable);
                    }
                });
            } else {
                //se non ho l'immagine Glide non deve più occuparsene
                Glide.clear(image);
                image.setImageResource(R.drawable.ic_user_noimg);
            }

            this.position = position;
            this.current = current;

        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, position);
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}

