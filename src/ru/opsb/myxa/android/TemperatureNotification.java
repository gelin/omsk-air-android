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
public class TemperatureNotification implements Constants {

    /** Notification ID */
    static final int ID = 1;
    /** Temperature image prefix */
    static final String RES_PREFIX="temp";

    public static void update(Context context, Bundle values) {
        Log.d(TAG, "updating notification");
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (!isEnabled(context)) {
            manager.cancel(ID);
            return;
        }
        float temp = values.getFloat(TEMPERATURE);
        if (Float.isNaN(temp)) {
            manager.cancel(ID);
            return;
        }
        manager.notify(ID, build(context, values));
    }

    /**
     *  Returns true if the notification is enabled.
     */
    public static boolean isEnabled(Context context) {
        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(NOTIFICATION, false);
    }

    static Notification build(Context context, Bundle values) {
        Notification.Builder builder = new Notification.Builder(context);
        float temp = values.getFloat(TEMPERATURE);
        long lastModified = values.getLong(LAST_MODIFIED);
        Resources res = context.getResources();
        builder.setSmallIcon(getIconResource(context, res, temp));
        builder.setTicker(formatTicker(res, temp));
        builder.setWhen(lastModified);
        builder.setOngoing(true);
        builder.setContentTitle(formatTitle(res, temp));
        builder.setContentText(formatText(res, temp));
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        return builder.getNotification();
    }

    static int getIconResource(Context context, Resources res, float temperature) {
        int temp = (int)temperature;
        if (temp < -50) {
            temp = -50;
        }
        if (temp > 50) {
            temp = 50;
        }
        StringBuilder resName = new StringBuilder();

        resName.append(RES_PREFIX);

        if (temp < 0) {
            resName.append("_minus");
        } else if (temp > 0) {

            resName.append("_plus");
        }
        resName.append("_").append(String.valueOf(Math.abs(temp)));
        Log.d(TAG, "notification image: " + resName);
        return res.getIdentifier(resName.toString(),
                "drawable", R.class.getPackage().getName());
    }

    static String formatTicker(Resources res, float temperature) {
        if (Math.abs(temperature) < 1) {
            return res.getString(R.string.notification_ticker_zero);
        }
        return res.getString(R.string.notification_ticker, (int)temperature);
    }

    static String formatTitle(Resources res, float temperature) {
        if (Math.abs(temperature) <= 0.1f) {
            return res.getString(R.string.notification_title_zero);
        }
        return res.getString(R.string.notification_title, temperature);
    }

    static String formatText(Resources res, float temperature) {
        return res.getString(R.string.notification_text);
    }

}
