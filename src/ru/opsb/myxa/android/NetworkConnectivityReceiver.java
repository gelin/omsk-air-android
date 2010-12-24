package ru.opsb.myxa.android;

import ru.opsb.myxa.android.periods.Period;
import ru.opsb.myxa.android.periods.PeriodFactory;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 *  Broadcast receiver which receives event about changes of network connectivity.
 *  Starts UpdateService if the network goes up.
 */
public class NetworkConnectivityReceiver extends BroadcastReceiver
        implements Constants {

    public void onReceive (Context context, Intent intent) {
        boolean noConnection = 
            intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        if (noConnection) {
            return;
        }
        NetworkInfo info = 
            (NetworkInfo)intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
        if (info == null) {
            return;
        }
        if (!info.isAvailable()) {
            return;
        }
        Log.d(TAG, "network is up");
        if (!isExpired(context)) {
            return;
        }
        context.startService(UpdateService.UPDATE_ALL_INTENT);
    }
    
    /**
     *  Checks if the temperature values expired.
     *  We're doing this check here to avoid unnecessary starting
     *  of UpdateService on each connectivity change.
     */
    static boolean isExpired(Context context) {
        SharedPreferences config =
                PreferenceManager.getDefaultSharedPreferences(context);
        Period period = PeriodFactory.createPeriod(
                config.getString(REFRESH, PeriodFactory.ONE_HOUR_PERIOD));
        SharedPreferences preferences =
                context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        PreferencesStorage storage = new PreferencesStorage(preferences);
        Bundle values = storage.get();
        long lastModified = values.getLong(LAST_MODIFIED);
        return period.isExpired(lastModified);
    }

}
