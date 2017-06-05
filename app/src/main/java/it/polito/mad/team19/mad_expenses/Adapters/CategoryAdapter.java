package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by ikkoyeah on 04/06/17.
 */

public class CategoryAdapter  extends BaseAdapter {

    private ArrayList<String> categories = new ArrayList<>();
    private Activity context;
    private ImageView img;
    private TextView name;

    public CategoryAdapter(Activity context, ArrayList<String> categories) {
        this.categories = categories;
        this.context = context;
    }

    public int getItemPositionByName(String name) {
        for(int i = 0; i < categories.size(); i++) {
            if(categories.get(i).equals(name))
                return i;
        }
        return 0;
    }

    @Override
    public int getCount() {
            return categories.size();
    }

    @Override
    public Object getItem(int position) {
        return categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.category_list_row, parent, false);
        }

        img = (ImageView) convertView.findViewById(R.id.icon);
        name = (TextView) convertView.findViewById(R.id.name);

        if (categories.get(position) == null) {
            img.setImageResource(R.drawable.ic_expense_2);
        }
        else
        {
            if (categories.get(position).trim().equals("transport")) {
                img.setImageResource(R.mipmap.ic_transport);
                name.setText(context.getResources().getString(R.string.transport));
            }


            if (categories.get(position).trim().equals("house")) {
                img.setImageResource(R.mipmap.ic_house);
                name.setText(context.getResources().getString(R.string.rent));
            }

            if (categories.get(position).trim().equals("food")) {
                img.setImageResource(R.mipmap.ic_food);
                name.setText(context.getResources().getString(R.string.food));
            }


            if (categories.get(position).trim().equals("drink")) {
                img.setImageResource(R.mipmap.ic_drink);
                name.setText(context.getResources().getString(R.string.drink));
            }

            if (categories.get(position).trim().equals("shopping")) {
                img.setImageResource(R.mipmap.ic_shopping);
                name.setText(context.getResources().getString(R.string.shopping));
            }
            if (categories.get(position).trim().equals("other"))
            {
                img.setImageResource(R.mipmap.ic_other);
                name.setText(context.getResources().getString(R.string.other));
            }
        }

        return convertView;
    }



}
