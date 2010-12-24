package ru.opsb.myxa.android;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import ru.opsb.myxa.android.periods.Period;
import ru.opsb.myxa.android.periods.PeriodFactory;
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
import android.preference.PreferenceManager;
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
     *  Intent to start this service to update all widgets and notifications.
     */
    public static final Intent UPDATE_ALL_INTENT = new Intent(ACTION_UPDATE_ALL);
    static {
        UPDATE_ALL_INTENT.setClassName(
                UpdateService.class.getPackage().getName(),
                UpdateService.class.getName());
    }

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

    /** Updater for the temperature values. */
    TemperatureUpdater updater;

    /** Update period */
    Period period;

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

        SharedPreferences config =
                PreferenceManager.getDefaultSharedPreferences(this);
        period = PeriodFactory.createPeriod(
                config.getString(REFRESH, PeriodFactory.ONE_HOUR_PERIOD));

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
            updateAll(this, oldValues); //immediately update widgets with old values
            requestUpdate(ids);     //update the same IDs later, when new temperature values come
        }

        shedulerNextRun(oldValues);

        synchronized (lock) {
            if (threadRunning) {
                return;     // only start processing thread if not already running
            }
            if (!isExpired(oldValues)) {
                stopSelf();     // temperature values are not expired
                Log.d(TAG, "skipping update, not expired");
                return;
            }
            if (!hasMoreUpdates() && !TemperatureNotification.isEnabled(this)) {
                stopSelf();     //no widgets or notification to update.
                Log.d(TAG, "skipping update, no widgets or notification");
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
                updateAll(UpdateService.this, values);
            }
        }
    };

    /**
     *  Sets alarm to next run.
     */
    void shedulerNextRun(Bundle values) {
        long nextUpdate = getNextStart(values);

        PendingIntent pendingIntent =
                PendingIntent.getService(this, 0, UPDATE_ALL_INTENT, 0);

        AlarmManager alarmManager = (AlarmManager)getSystemService(
                Context.ALARM_SERVICE);
        if (nextUpdate > 0) {
            Log.d(TAG, "scheduling update to " + new Date(nextUpdate));
            alarmManager.set(AlarmManager.RTC, nextUpdate, pendingIntent);
        } else {
            Log.d(TAG, "cancelling update");
            alarmManager.cancel(pendingIntent);
        }
    }

    /**
     *  Immediately updates widgets and notification bar.
     */
    static void updateAll(Context context, Bundle values) {
        updateNotification(context, values);
        updateWidgets(context, values);
    }

    /**
     *  Immediately updates notification in the status bar.
     */
    static void updateNotification(Context context, Bundle values) {
        TemperatureNotification.update(context, values);
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
    boolean isExpired(Bundle values) {
        long lastModified = values.getLong(LAST_MODIFIED);
        return period.isExpired(lastModified);
    }

    /**
     *  Calculates next update time.
     */
    long getNextStart(Bundle values) {
        long lastModified = values.getLong(LAST_MODIFIED);
        return period.getNextStart(lastModified);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}