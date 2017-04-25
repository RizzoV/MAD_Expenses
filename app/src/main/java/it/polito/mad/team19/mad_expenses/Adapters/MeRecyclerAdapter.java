package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Classes.Me;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by ikkoyeah on 31/03/17.
 */

public class MeRecyclerAdapter extends RecyclerView.Adapter<MeRecyclerAdapter.MyViewHolder>
{

    ArrayList<Me> me;
    Activity context;
    private LayoutInflater mInflater;

    public MeRecyclerAdapter(Context context, ArrayList<Me> me)
    {
        this.me = me;
        this.context = (Activity) context;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = mInflater.inflate(R.layout.me_list_fromto_row, parent, false);
        MyViewHolder holder = new MeRecyclerAdapter.MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position)
    {
        Me currentObj = me.get(position);
        holder.setData(currentObj,position);
    }


    @Override
    public int getItemCount()
    {
        return me.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {

        ImageView image;
        TextView name;
        TextView amount;
        int position;
        Me current;

        public MyViewHolder(View itemView)
        {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.me_image);
            name = (TextView) itemView.findViewById(R.id.me_fromto);
            amount = (TextView) itemView.findViewById(R.id.me_amount);

        }

        public void setData (Me current, int position)
        {
            this.name.setText(current.getName());
            this.amount.setText(current.getCurrency().getSymbol().toString() + " " + String.format("%.2f", current.getAmount()));
            if((position%2)==0)
                this.image.setImageResource(R.drawable.man4);
            else
                this.image.setImageResource(R.drawable.girl4);

            this.position = position;
            this.current = current;

        }
    }
}

