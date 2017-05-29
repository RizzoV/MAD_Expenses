package it.polito.mad.team19.mad_expenses.Adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by Valentino on 29/05/2017.
 */

public class CurrenciesAdapter extends BaseAdapter implements Filterable {

    protected ArrayList<String> originalCurrenciesList = new ArrayList<>();
    private ArrayList<String> filteredList = new ArrayList<>();
    private Activity context;
    private CurrenciesFilter mFilter = new CurrenciesFilter();

    private TextView currencySymbol;
    private TextView currencyCode;

    public CurrenciesAdapter(Activity context, ArrayList<String> currenciesList) {
        this.originalCurrenciesList = currenciesList;
        this.filteredList = currenciesList;
        this.context = context;
    }

    @Override
    public int getCount() {
        if(filteredList != null)
            return filteredList.size();
        else
            return originalCurrenciesList.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.currencies_list_row, parent, false);
        }

        currencySymbol = (TextView) convertView.findViewById(R.id.currency_name);
        currencyCode = (TextView) convertView.findViewById(R.id.currency_symbol);

        String fields[] = filteredList.get(position).split("\t ");
        // Fields are currencyCode, currencyCode
        currencyCode.setText(fields[0]);
        if(fields.length > 1)
            currencySymbol.setText(fields[1]);
        else
            currencySymbol.setText("");

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public class CurrenciesFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<String> list = originalCurrenciesList;

            int count = list.size();
            final ArrayList<String> nlist = new ArrayList<String>(count);

            String filterableString ;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i);
                if (filterableString.trim().toLowerCase().contains(filterString)) {
                    nlist.add(filterableString);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<String>) results.values;
            notifyDataSetChanged();
        }
    }
}
