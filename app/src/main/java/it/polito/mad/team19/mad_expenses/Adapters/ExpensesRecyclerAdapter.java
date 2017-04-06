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

import it.polito.mad.team19.mad_expenses.Classes.Expense;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by Jured on 31/03/17.
 */

public class ExpensesRecyclerAdapter extends RecyclerView.Adapter<ExpensesRecyclerAdapter.MyViewHolder> {

    ArrayList<Expense> expenses;
    Activity context;
    private LayoutInflater mInflater;

    public ExpensesRecyclerAdapter(Context context, ArrayList<Expense> expenses) {
        this.expenses = expenses;
        this.context = (Activity) context;
        this.mInflater = LayoutInflater.from(this.context);
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

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView name;
        TextView amount;
        int position;
        Expense current;
        TextView description;

        public MyViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.expense_imageView);
            name = (TextView) itemView.findViewById(R.id.expense_name_tv);
            amount = (TextView) itemView.findViewById(R.id.expense_cost_amount_tv);
            description = (TextView) itemView.findViewById(R.id.expense_description_tv);
        }

        public void setData (Expense current, int position) {
            this.name.setText(current.getName());
            this.amount.setText(current.getCurrency().getSymbol().toString() + " " + String.format("%.2f", current.getCost()));
            this.image.setImageResource(R.drawable.circle);
            this.position = position;
            this.current = current;
            this.description.setText(current.getDescritpion());
        }
    }
}
