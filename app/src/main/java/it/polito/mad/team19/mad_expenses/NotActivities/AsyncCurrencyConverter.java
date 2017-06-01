package it.polito.mad.team19.mad_expenses.NotActivities;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AsyncCurrencyConverter extends AsyncTask<Void, Integer, Double> {

    private String toCurrency;
    private Context context;
    protected static final String currenciesFileName = "conversions.json";


    public AsyncCurrencyConverter(Context context, String toCurrency) {
        this.context = context;
        this.toCurrency = toCurrency;
    }


    @Override
    protected Double doInBackground(Void... vo) {

        FileOutputStream fos;
        FileInputStream fis;
        JSONObject json;
        JSONObject jsonIn;
        String[] filesList;
        Boolean found = false;
        String jsonString;
        Double exchangeRate = -1d;

        filesList = context.fileList();

        // Controlla se il file esiste già o meno
        for (String fileName : filesList) {
            if (fileName.equals(currenciesFileName)) {
                found = true;
                break;
            }
        }

        // Se il file non esiste, crealo
        if (!found) {
            try {
                fos = context.openFileOutput(currenciesFileName, Context.MODE_PRIVATE);
                jsonIn = getJson("https://api.fixer.io/latest");
                if(jsonIn == null) {
                    Log.e("AsyncCurrencyConverter", "ERRORE: jsonIn è NULL!");
                }
                fos.write(jsonIn.toString().getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Leggi il valore della conversione
        try {
            // Leggi il contenuto del file
            fis = context.openFileInput(currenciesFileName);
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            jsonString = new String(buffer, "UTF-8");

            // Convertilo in un oggetto JSON
            json = new JSONObject(jsonString);

            // Ricava il valore della conversione
            if(json.getJSONObject("rates").getString(toCurrency) != null)
                exchangeRate = Double.parseDouble(json.getJSONObject("rates").getString(toCurrency));

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return exchangeRate;
    }


    private JSONObject getJson(String url) {

        String result = "";
        JSONObject jsonObject = null;
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                        .url(url)
                        .build();

            Response response = client.newCall(request).execute();
            result = response.body().string();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }

        // Convert string to jsonObject
        try {
            jsonObject = new JSONObject(result);
        } catch(JSONException e) {
            return null;
        }

        return jsonObject;
    }
}
