package ru.opsb.myxa.android;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

/**
 *  Widget which displays temperature.
 *  Widget update code copied from http://code.google.com/p/android-sky/
 */
public class TemperatureWidget extends AppWidgetProvider 
        implements Constants {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        //if no specific widgets requested, collect list of all
        if (appWidgetIds == null) {
            appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, TemperatureWidget.class));
        }
        
        //update with old values
        PreferencesStorage storage = new PreferencesStorage(
                context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE));
        RemoteViews views = buildUpdate(context, storage.get());
        appWidgetManager.updateAppWidget(appWidgetIds, views);
        
        //request update for these widgets and launch updater service
        UpdateService.requestUpdate(appWidgetIds);
        context.startService(new Intent(context, UpdateService.class));
    }

    /**
     *  Creates new updated views for the widgets.
     *  @param  context context to retrieve remote views
     *  @param  values  values of the temperature to update
     */
    public static RemoteViews buildUpdate(Context context, Bundle values) {
        int layout = R.layout.widget;
        
        SharedPreferences prefs = 
            PreferenceManager.getDefaultSharedPreferences(context);
        if (HTC.equals(prefs.getString(STYLE, ""))) {
            layout = R.layout.htc_widget;
        }
        
        RemoteViews views = new RemoteViews(
                context.getPackageName(), layout);
        setUpOnClick(context, views);
        updateWidgetViews(context, views, values);
        return views;
    }
    
    static void setUpOnClick(Context context, RemoteViews views) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);
    }
    
    static void updateWidgetViews(Context context, RemoteViews views, Bundle values) {
        float temp = values.getFloat(TEMPERATURE, Float.NaN);
        views.setTextViewText(R.id.temp_value, formatTemperature(context, temp));
        //for test
        /*
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(values.getLong(LAST_MODIFIED));
        views.setTextViewText(R.id.temp_value, 
                String.valueOf(calendar.get(Calendar.MINUTE)));
        */
        //views.setTextViewText(R.id.temp_value, formatTemperature(context, -0.5f));
    }
    
    static String formatTemperature(Context context, float temperature) {
        if (Float.isNaN(temperature)) {
            return context.getResources().getString(R.string.no_temp);
        } else if (Math.abs(temperature) <= 1) {
            return context.getResources().getString(R.string.widget_zero_temp);
        }
        return context.getResources().getString(
                R.string.widget_temp_format, (int)temperature);
    }

}
