package it.polito.mad.team19.mad_expenses.Classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ikkoyeah on 22/05/17.
 */

public class StartBackgroundNotificationService extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context,NotificationService.class);
        context.startService(i);
    }
}
