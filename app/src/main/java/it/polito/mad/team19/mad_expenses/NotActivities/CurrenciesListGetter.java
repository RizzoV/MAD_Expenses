package it.polito.mad.team19.mad_expenses.NotActivities;


import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by Valentino on 01/06/2017.
 */

public class CurrenciesListGetter {

    private Context context;

    public CurrenciesListGetter(Context context) {
        this.context = context;
    }

    public ArrayList<String> getAvailableCurrenciesCodes() {
        ArrayList<String> currenciesList = new ArrayList<>();
        String[] filesList = context.fileList();
        Boolean found = false;

        // Controlla se il file esiste già o meno
        for (String fileName : filesList) {
            if (fileName.equals(AsyncCurrencyConverter.currenciesFileName)) {
                found = true;
                break;
            }
        }

        if(!found) {
            // Se il file delle conversioni non c'è, fai una conversione fittizia per crearlo
            try {
                (new AsyncCurrencyConverter(context, "USD")).execute().get();
            } catch (InterruptedException e) {
                Log.e("CurrenciesListGetter", "InterruptedException: " + e.getMessage());
            } catch (ExecutionException e) {
                Log.e("CurrenciesListGetter", "ExecutionException: " + e.getMessage());
            }
        }

        // Ora che il file c'è sicuramente, procedi
        try {
            // Leggi il contenuto del file
            FileInputStream fis = context.openFileInput(AsyncCurrencyConverter.currenciesFileName);
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            String jsonString = new String(buffer, "UTF-8");

            // Convertilo in un oggetto JSON
            JSONObject json = new JSONObject(jsonString);

            // Prendi la lista di exchange rates
            Iterator<String> iter = json.getJSONObject("rates").keys();
            while (iter.hasNext()) {
                String currencyCode = iter.next();
                currenciesList.add(currencyCode);
            }


            } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            Log.e("CurrenciesListGetter", "IOException: " + e1.getMessage());
        } catch (JSONException e1) {
            Log.e("CurrenciesListGetter", "JSONException: " + e1.getMessage());
        }

        return currenciesList;
    }

    public Set<Currency> getAvailableCurrencies() {

        ArrayList<String> currenciesCodes = getAvailableCurrenciesCodes();

        Set<Currency> filteredSet = new HashSet<>();
        Set<Currency> initialSet = Currency.getAvailableCurrencies();
        for(Currency c : initialSet) {
            for(String code : currenciesCodes) {
                if (c.getCurrencyCode().equals(code)) {
                    filteredSet.add(c);
                    break;
                }
            }
        }

        filteredSet.add(Currency.getInstance(Locale.ITALY));

        return filteredSet;
    }


}
