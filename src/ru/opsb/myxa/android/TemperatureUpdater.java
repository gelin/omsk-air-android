package ru.opsb.myxa.android;

import java.util.Calendar;

import ru.opsb.myxa.android.periods.Period;
import ru.opsb.myxa.android.periods.PeriodFactory;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 *  Requests the server for new temperature values and saves new
 *  values to preferences.
 *  If less than one minuted comes from the last update, returns
 *  old values immediately.
 */
public class TemperatureUpdater implements Runnable, Constants {

    /** Storage for the previous temperature values */
    PreferencesStorage storage;
    /** Handler to receive new temperature values */
    Handler handler;
    
    /**
     *  Creates the updater
     *  @param handler  handler to which a message with new values or error
     *                  will be sent
     *  @param preferences  preferences where temperature values are saved
     */
    public TemperatureUpdater(Handler handler, SharedPreferences preferences) {
        this.handler = handler;
        this.storage = new PreferencesStorage(preferences);
    }
    
    /**
     *  Implements update process.
     */
    public void run() {
        Bundle values = storage.get();
        TemperatureGetter getter = 
                new TemperatureGetter(internalHandler, values);
        if (isExpired(values)) {
            getter.run();
        } else {
            TemperatureGetter.sendResult(handler, values);    //send old values as updated
        }
    }
    
    /**
     *  Returns true if the temperature values are expired,
     *  i.e. more that 1 minute come from the last update.
     */
    static boolean isExpired(Bundle values) {
        long lastModified = values.getLong(LAST_MODIFIED);
        Period period = PeriodFactory.createPeriod(PeriodFactory.ONE_MINUTE_PERIOD);
        return period.isExpired(lastModified);
    }

    /**
     *  Handles temperature updates.
     */
    final Handler internalHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == TEMPERATURE_UPDATE) {
                Bundle values = msg.getData();
                storage.put(values);
                Log.i(TAG, "temperature: " + values.getFloat(TEMPERATURE));
            }
            handler.handleMessage(msg);     //propagate message
        }
    };

}
