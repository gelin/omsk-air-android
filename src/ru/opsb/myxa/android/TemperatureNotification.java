package ru.opsb.myxa.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 *  Represents temperature notification.
 */
public class TemperatureNotification extends Notification 
        implements Constants {

    /** Notification ID */
    static final int ID = 1;
    
    public static void update(Context context, Bundle values) {
        Log.d(TAG, "updating notification");
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        SharedPreferences prefs = 
            PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean(NOTIFICATION, false)) {
            manager.cancel(ID);
            return;
        }
        float temp = values.getFloat(TEMPERATURE);
        if (Float.isNaN(temp)) {
            manager.cancel(ID);
            return;
        }
        manager.notify(ID, new TemperatureNotification(context, values));
    }
    
    TemperatureNotification(Context context, Bundle values) {
        float temp = values.getFloat(TEMPERATURE);
        long lastModified = values.getLong(LAST_MODIFIED);
        Resources res = context.getResources();
        this.icon = R.drawable.temp_0;
        this.tickerText = formatTicker(res, temp);
        this.when = lastModified;
        this.flags |= FLAG_NO_CLEAR;
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        setLatestEventInfo(context, formatTitle(res, temp), 
                formatText(res, temp), pendingIntent);
    }
    
    static String formatTicker(Resources res, float temperature) {
        if (Math.abs(temperature) <= 1) {
            return res.getString(R.string.notification_ticker_zero);
        }
        return res.getString(R.string.notification_ticker, (int)temperature);
    }
    
    String formatTitle(Resources res, float temperature) {
        if (Math.abs(temperature) <= 0.1f) {
            return res.getString(R.string.notification_title_zero);
        }
        return res.getString(R.string.notification_title, temperature);
    }
    
    String formatText(Resources res, float temperature) {
        return res.getString(R.string.notification_text);
    }

}