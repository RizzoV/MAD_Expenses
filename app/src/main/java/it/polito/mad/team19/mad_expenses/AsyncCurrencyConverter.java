package it.polito.mad.team19.mad_expenses;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Classes.BalanceCalculator;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.fx.FxQuote;

/**
 * Created by Jured on 17/05/17.
 */

public class AsyncCurrencyConverter extends AsyncTask<Void, Integer, Float> {

    private String fromCurrency;
    private String toCurrency;
    private FxQuote exchangeRate = null;

    public AsyncCurrencyConverter(String fromCurrency, String toCurrency) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
    }

    @Override
    protected Float doInBackground(Void... vo) {

        try {
            exchangeRate = YahooFinance.getFx(fromCurrency + toCurrency +"=X");
            if(exchangeRate.getPrice() != null)
                return exchangeRate.getPrice().floatValue();
            else
                return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
