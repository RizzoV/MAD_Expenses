package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Classes.Expense;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by Jured on 27/03/17.
 */

public class ExpensesAdapter extends BaseAdapter
{

    ArrayList<Expense> expenses;
    Activity context;

    public ExpensesAdapter(Context context, ArrayList<Expense> expenses)
    {
        this.expenses = expenses;
        this.context = (Activity) context;
    }

    @Override
    public int getCount() {
        return expenses.size();
    }

    @Override
    public Object getItem(int position) {
        return expenses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView==null)
        {
            convertView=context.getLayoutInflater().inflate(R.layout.expenses_list_row,parent,false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.expense_name_tv);
        TextView cost = (TextView) convertView.findViewById(R.id.expense_cost_amount_tv);
        TextView description = (TextView) convertView.findViewById(R.id.expense_description_tv);

        Expense expense = expenses.get(position);

        name.setText(expense.getName());
        cost.setText(expense.getCurrency().getSymbol().toString() + " " + String.format("%.2f", expense.getCost()));
        description.setText(expense.getDescritpion());

        return convertView;
    }
}
