package ru.opsb.myxa.android;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
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
        Updater updater = new Updater(context);
        context.bindService(new Intent(UpdateServiceInterface.class.getName()),
                updater, Context.BIND_AUTO_CREATE);
    }
    
    /**
     *  Handles connection with the service.
     */
    static class Updater extends UpdateServiceCallback.Stub 
            implements ServiceConnection {

        Context context;
        UpdateServiceInterface service;
        
        public Updater(Context context) {
            this.context = context;
        }
        
        public void onServiceConnected(ComponentName className, IBinder binder) {
            service = UpdateServiceInterface.Stub.asInterface(binder);
            if (service != null) {
                try {
                    service.registerCallback(this);
                    service.startUpdate();
                } catch (RemoteException e) {
                    Log.w(TAG, "failed to start update", e);
                }
            }
        }
        public void onServiceDisconnected(ComponentName name) {
            service = null;
        }

        public void onTemperatureUpdate(Bundle values) {
            RemoteViews views = new RemoteViews(
                    context.getPackageName(), R.layout.appwidget);
            updateWidgetViews(context, views, values);
        }
        public void onError(String error) {
            //nothing to do on widget
        }

    }

}
