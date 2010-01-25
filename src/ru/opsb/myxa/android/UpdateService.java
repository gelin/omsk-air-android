package ru.opsb.myxa.android;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

/**
 *  Updates widget after receiving new temperature.
 *  Widget update code copied from http://code.google.com/p/android-sky/
 */
public class UpdateService extends Service implements Constants, Runnable {

    /**
     *  Specific {@link Intent#setAction(String)} used when performing a full
     *  update of all widgets, usually when an update alarm goes off.
     */
    public static final String ACTION_UPDATE_ALL = 
            UpdateService.class.getPackage() + ".UPDATE_ALL";
    
    /**
     *  Lock used when maintaining queue of requested updates.
     */
    private static Object lock = new Object();
    
    /**
     *  Flag if there is an update thread already running. We only launch a new
     *  thread if one isn't already running.
     */
    private static boolean threadRunning = false;
    
    /**
     *  Internal queue of requested widget updates. You <b>must</b> access
     *  through {@link #requestUpdate(int[])} or {@link #getNextUpdate()} to make
     *  sure your access is correctly synchronized.
     */
    private static Queue<Integer> widgetIds = new LinkedList<Integer>();
    
    /**
     *  Updater for the temperature values.
     */
    TemperatureUpdater updater;
    
    /**
     *  Request updates for the given widgets. Will only queue them up, you are
     *  still responsible for starting a processing thread if needed, usually by
     *  starting the parent service.
     */
    public static void requestUpdate(int[] appWidgetIds) {
        synchronized (lock) {
            for (int appWidgetId : appWidgetIds) {
                widgetIds.add(appWidgetId);
            }
        }
    }
    

    /**
     *  Peek if we have more updates to perform. This method is special because
     *  it assumes you're calling from the update thread, and that you will
     *  terminate if no updates remain. (It atomically resets
     *  {@link #threadRunning} when none remain to prevent race conditions.)
     */
    private static boolean hasMoreUpdates() {
        synchronized (lock) {
            boolean hasMore = !widgetIds.isEmpty();
            if (!hasMore) {
                threadRunning = false;
            }
            return hasMore;
        }
    }

    /**
     *  Poll the next widget update in the queue.
     */
    private static int getNextUpdate() {
        synchronized (lock) {
            if (widgetIds.peek() == null) {
                return AppWidgetManager.INVALID_APPWIDGET_ID;
            } else {
                return widgetIds.poll();
            }
        }
    }
    
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        SharedPreferences preferences = 
                getSharedPreferences(PREFERENCES, MODE_PRIVATE); 
        PreferencesStorage storage = new PreferencesStorage(preferences);
        Bundle oldValues = storage.get();
        updater = new TemperatureUpdater(handler, preferences);
        
        // if requested, trigger update of all widgets
        if (ACTION_UPDATE_ALL.equals(intent.getAction())) {
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            int[] ids = manager.getAppWidgetIds(
                    new ComponentName(this, TemperatureWidget.class));
            requestUpdate(ids);
            updateWidgets(this, oldValues); //immediately update widgets with old values
            requestUpdate(ids);     //update the same IDs later, when new temperature values come
        }

        shedulerNextRun();

        synchronized (lock) {
            if (threadRunning) {
                return;     // only start processing thread if not already running
            }
            if (!isExpired(oldValues)) {
                stopSelf();     // temperature values are not expired 
                return;
            }
            if (!threadRunning) {
                threadRunning = true;
                new Thread(this).start();
            }
        }
    }

    /**
     *  Main thread for running through any requested widget updates until none
     *  remain. Also sets alarm to perform next update.
     */
    public void run() {
        updater.run();
        stopSelf();
    }
    
    /**
     *  Handles temperature updates.
     */
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == TEMPERATURE_UPDATE) {
                Bundle values = msg.getData();
                updateWidgets(UpdateService.this, values);
            }
        }
    };
    
    /**
     *  Sets alarm to next run.
     */
    void shedulerNextRun() {
        long nextUpdate = getNextUpdate(System.currentTimeMillis());
        
        Intent updateIntent = new Intent(ACTION_UPDATE_ALL);
        updateIntent.setClass(this, UpdateService.class);

        PendingIntent pendingIntent = 
                PendingIntent.getService(this, 0, updateIntent, 0);

        AlarmManager alarmManager = (AlarmManager)getSystemService(
                Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, nextUpdate, pendingIntent);  //RTC_WAKEUP ???
    }
    
    /**
     *  Immediately updates widgets with specified values.
     */
    static void updateWidgets(Context context, Bundle values) {
        Log.d(TAG, "updating widgets");
        
        AppWidgetManager manager = 
                AppWidgetManager.getInstance(context);
        RemoteViews updateViews = 
                TemperatureWidget.buildUpdate(context, values);

        if (updateViews != null) {
            while (hasMoreUpdates()) {
                int appWidgetId = getNextUpdate();
                manager.updateAppWidget(appWidgetId, updateViews);
            }
        }
    }

    /**
     *  Checks is the temperature values expired.
     *  Expiration period here is one hour.
     */
    static boolean isExpired(Bundle values) {
        long lastModified = values.getLong(LAST_MODIFIED);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);    //this hour with zero minutes
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return lastModified < calendar.getTimeInMillis();   //strongly less! 
    }
    
    /**
     *  Calculates next update time.
     */
    static long getNextUpdate(long now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        calendar.set(Calendar.MINUTE, 0);    //this hour with zero minutes
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.HOUR_OF_DAY, 1);  //next hour
        //calendar.add(Calendar.MINUTE, 1);  //next minute
        return calendar.getTimeInMillis();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}