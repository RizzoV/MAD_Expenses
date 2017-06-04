package it.polito.mad.team19.mad_expenses.NotActivities;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

        JSONObject json;
        String[] filesList;
        Boolean found = false;
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
            createCurrenciesJSONFile();
        }


        try {
            // Se il file esiste ma ha dimensione 0, riscaricalo
            FileInputStream fis = context.openFileInput(currenciesFileName);
            if(fis.available() == 0) {
                createCurrenciesJSONFile();
            }

            json = readJSONFile();
            if(json == null) {
                Log.e("AsyncCurrencyConverter", "The JSON file was null - 1");
                return exchangeRate;
            }

            // Check data ultimo download
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String currentDate = sdf.format(today);
            String jsonDate = json.getString("date");
            if(!jsonDate.equals(currentDate)) {
                /* Allora il file non è aggiornato
                 * A sto punto la questione è questa: può essere che l'API mi dia in data X un file aggiornato
                 * al giorno X-1, negli orari notturni in particolare, finchè non vengono aggiornate le valute.
                 * Qualora dunque la data non fosse aggiornata, riscarica solo se la differenza è di 3 giorni almeno
                 */
                String[] jsonDateFields = jsonDate.split("-");
                String[] currentDateFields = currentDate.split("-");
                if(Integer.parseInt(currentDateFields[2]) - Integer.parseInt(jsonDateFields[2]) > 3) {
                    createCurrenciesJSONFile();
                    json = readJSONFile();
                    if (json == null) {
                        Log.e("AsyncCurrencyConverter", "The JSON file was null - 2");
                        return exchangeRate;
                    }
                }
            }

            // Ricava il valore della conversione
            if (json.getJSONObject("rates").getString(toCurrency) != null)
                exchangeRate = Double.parseDouble(json.getJSONObject("rates").getString(toCurrency));
        } catch (JSONException | IOException e) {
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
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // Convert string to jsonObject
        try {
            jsonObject = new JSONObject(result);
        } catch (JSONException e) {
            return null;
        }

        return jsonObject;
    }

    private void createCurrenciesJSONFile() {

        FileOutputStream fos;
        JSONObject jsonIn;
        try {
            fos = context.openFileOutput(currenciesFileName, Context.MODE_PRIVATE);
            jsonIn = getJson("https://api.fixer.io/latest");
            if (jsonIn == null) {
                Log.e("AsyncCurrencyConverter", "ERRORE: jsonIn è NULL!");
            }
            fos.write(jsonIn.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONObject readJSONFile() {

        FileInputStream fis;
        String jsonString;
        try {
            // Leggi il contenuto del file
            fis = context.openFileInput(currenciesFileName);
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            jsonString = new String(buffer, "UTF-8");

            // Convertilo in un oggetto JSON
            return new JSONObject(jsonString);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
