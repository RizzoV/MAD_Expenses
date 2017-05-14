package it.polito.mad.team19.mad_expenses.Classes;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by ikkoyeah on 13/05/17.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "NetworkChangeReceiver";
    AlertDialog alertDialog;
    private boolean isConnected = false;
    private boolean dialogShow;
    private View viewForSnackbar = null;
    private boolean isOpened = false;
    Context mContext;
    Snackbar snack;

    public void setDialogShowTrue(boolean dialogShow) {this.dialogShow = dialogShow;}
    public void setViewForSnackbar(View view){this.viewForSnackbar = view;}


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(LOG_TAG, "Receieved notification about network status");
        mContext = context;
        isNetworkAvailable(context);

    }

    public void closeDialog()
    {
        if(alertDialog!=null)
            alertDialog.dismiss();
    }

    public void closeSnack()
    {
        if(snack!=null)
            snack.dismiss();
    }



    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {

                        Log.v(LOG_TAG, "Now you are connected to Internet!");
                        if(alertDialog!=null) {
                            alertDialog.cancel();
                            alertDialog = null;
                        }

                        if(snack!=null)
                            snack.dismiss();

                        isConnected = true;
                        return true;
                    }
                }
            }
        }


        Log.v(LOG_TAG, "You are not connected to Internet!");
        if(dialogShow)
        {
            Log.v(LOG_TAG, "Dialog");
            if (alertDialog==null)
                showConnectionLostDialog(context);
            else
                alertDialog.show();
        }
        else
        {
            showSnackBar(context);
        }

        isConnected = false;

        return false;
    }

    public void showSnackBar(final Context mContext)
    {
        if(viewForSnackbar!=null)
        {
            Snackbar.make(viewForSnackbar, mContext.getString(R.string.noConnectionSnack), Snackbar.LENGTH_INDEFINITE)
                    .setAction(mContext.getString(R.string.openSettings), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            mContext.startActivity(intent);
                        }
                    })
                    .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        @Override
                        public void onShown(Snackbar transientBottomBar) {
                            super.onShown(transientBottomBar);
                            snack = transientBottomBar;
                        }
                    })
                    .show();
            isOpened = true;
        }

    }

    public void showConnectionLostDialog(final Context mContext)
    {
        alertDialog = new AlertDialog.Builder(mContext)
                .setMessage(mContext.getString(R.string.noConnectionDesc))
                .setPositiveButton(mContext.getString(R.string.openSettings),null)
                .setCancelable(false)
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button buttonPositive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                buttonPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(Settings.ACTION_SETTINGS);
                        mContext.startActivity(intent);
                    }
                });
            }
        });
        alertDialog.show();

    }
}
