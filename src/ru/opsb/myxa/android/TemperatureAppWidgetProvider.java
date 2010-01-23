package ru.opsb.myxa.android;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.RemoteViews;

public class TemperatureAppWidgetProvider extends AppWidgetProvider 
        implements Constants {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        
        RemoteViews views = new RemoteViews(
                context.getPackageName(), R.layout.appwidget);
        setUpOnClick(context, views);
        setOldValues(context, views);
        
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        
        startUpdate(context);
    }
    
    static void setUpOnClick(Context context, RemoteViews views) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);
    }
    
    static void setOldValues(Context context, RemoteViews views) {
        PreferencesStorage storage = new PreferencesStorage(
                context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE));
        Bundle values = storage.get();
        updateWidgetViews(context, views, values);
    }
    
    static void updateWidgetViews(Context context, RemoteViews views, Bundle values) {
        float temp = values.getFloat(TEMPERATURE, Float.NaN);
        views.setTextViewText(R.id.temp_value, formatTemperature(context, temp));
    }
    
    static String formatTemperature(Context context, float temperature) {
        if (Float.isNaN(temperature)) {
            return context.getResources().getString(R.string.no_temp);
        }
        return context.getResources().getString(
                R.string.widget_temp_format, temperature);
    }

    static void startUpdate(Context context) {
        context.startService(new Intent(context, UpdateService.class));
    }
    
    /**
     *  Updates widget after receiving new temperature.
     */
    public static class UpdateService extends Service {

        @Override
        public void onStart(Intent intent, int startId) {
            RemoteViews views = new RemoteViews(
                    getPackageName(), R.layout.appwidget);
            Handler handler = new UpdateHandler(this, views);
            TemperatureUpdater updater = new TemperatureUpdater(
                    handler, getSharedPreferences(PREFERENCES, MODE_PRIVATE));
            updater.run();
            
            ComponentName thisWidget = 
                    new ComponentName(this, TemperatureAppWidgetProvider.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, views);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

    }
    
    /**
     *  Handles temperature updates.
     */
    static class UpdateHandler extends Handler {
        
        Context context;
        RemoteViews views;
        
        public UpdateHandler(Context context, RemoteViews views) {
            this.context = context;
            this.views = views;
        }
        
        public void handleMessage(Message msg) {
            if (msg.what == TEMPERATURE_UPDATE) {
                Bundle values = msg.getData();
                updateWidgetViews(context, views, values);
            }
        }
        
    }

}
