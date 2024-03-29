package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import it.polito.mad.team19.mad_expenses.Classes.Expense;
import it.polito.mad.team19.mad_expenses.R;
import it.polito.mad.team19.mad_expenses.TopicActivity;

/**
 * Created by Jured on 31/03/17.
 */

public class ExpensesRecyclerAdapter extends RecyclerView.Adapter<ExpensesRecyclerAdapter.MyViewHolder>{

    private ArrayList<Expense> expenses;
    private Activity context;
    private LayoutInflater mInflater;
    //LUDO: aggiunto metodo onItemClickListener
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;
    private String groupId;
    private Double exchangeRate;
    private String currencySymbol;

    public ExpensesRecyclerAdapter(Context context, ArrayList<Expense> expenses, String groupId, String currencySymbol, Double exchangeRate) {
        this.expenses = expenses;
        this.context = (Activity) context;
        this.mInflater = LayoutInflater.from(this.context);
        this.groupId = groupId;
        this.exchangeRate = exchangeRate;
        this.currencySymbol = currencySymbol;
        this.exchangeRate = exchangeRate;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.expenses_list_row, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Expense currentObj = expenses.get(position);
        holder.setData(currentObj,position);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView image;
        TextView name;
        TextView amount;
        Button topic_btn;
        int position;
        Expense current;
        TextView description;
        View itemView;

        private MyViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.expense_imageView);
            name = (TextView) itemView.findViewById(R.id.expense_name_tv);
            amount = (TextView) itemView.findViewById(R.id.expense_cost_amount_tv);
            description = (TextView) itemView.findViewById(R.id.expense_description_tv);
            topic_btn = (Button) itemView.findViewById(R.id.topic_button);


            //LUDO: aggiunto metodo onItemClickListener
            this.itemView = itemView;
            itemView.setOnClickListener(this);
        }



        //LUDO: aggiunto metodo onItemClickListener
        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, position);
            }
        }

        private void setData(Expense current, int position) {
            this.name.setText(current.getName());
            this.amount.setText(String.format(Locale.getDefault(), "%.2f", current.getCost()) + " " + Currency.getInstance("EUR").getSymbol());
            //this.image.setImageResource(R.drawable.expenses_icon);


            //prendere da Firebase la categoria
            if (current.getCategory() == null) {
                this.image.setImageResource(R.drawable.ic_expense_2);
            }
            else
            {
                if (current.getCategory().trim().equals("transport")) {
                    this.image.setImageResource(R.mipmap.ic_transport);
                }


                if (current.getCategory().trim().equals("house")) {
                    this.image.setImageResource(R.mipmap.ic_house);
                }

                if (current.getCategory().trim().equals("food")) {
                    this.image.setImageResource(R.mipmap.ic_food);
                }

                if (current.getCategory().trim().equals("drink")) {
                    this.image.setImageResource(R.mipmap.ic_drink);
                }

                if (current.getCategory().trim().equals("shopping")) {
                    this.image.setImageResource(R.mipmap.ic_shopping);
                }
                if (current.getCategory().trim().equals("other"))
                {
                    this.image.setImageResource(R.mipmap.ic_other);
                }
            }

            this.amount.setText(String.format(Locale.getDefault(), "%.2f", current.getCost() * exchangeRate) + " " + currencySymbol);
            this.position = position;
            this.current = current;
            this.description.setText(current.getDescritpion());

            this.topic_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, TopicActivity.class);
                    i.putExtra("topicType","expenses");
                    i.putExtra("topicName",current.getName());
                    i.putExtra("groupId", groupId);
                    i.putExtra("expenseId", current.getFirebaseId());
                    context.startActivity(i);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view , int position);
    }

    public interface  OnItemLongClickListener {
        void onItemLongClick(View view , int position);
    }

    //Jured: onItemLongClickListener
    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void SetOnItemLongClickListener (final OnItemLongClickListener mItemLongClickListener){
        this.mItemLongClickListener = mItemLongClickListener;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

        holder.itemView.setLongClickable(true);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mItemLongClickListener.onItemLongClick(v, position);
                return false;
            }
        });
    }

}
