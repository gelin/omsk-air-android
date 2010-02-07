package ru.opsb.myxa.android.periods;

import ru.opsb.myxa.android.Constants;
import android.util.Log;

/**
 *  Creates different periods.
 */
public class PeriodFactory implements Constants {

    /** One minute period name */
    public static final String ONE_MINUTE_PERIOD = "1m";
    
    /** Fifteen minutes period name */
    public static final String FIFTEEN_MINUTES_PERIOD = "15m";
    
    /** Thirty minutes period name */
    public static final String THIRTY_MINUTES_PERIOD = "30m";
    
    /** One hour period name */
    public static final String ONE_HOUR_PERIOD = "1h";
    
    /** Three hour period name */
    public static final String THREE_HOURS_PERIOD = "3h";
    
    /**
     *  Creates period by name.
     */
    public static Period createPeriod(String name) {
        if (ONE_MINUTE_PERIOD.equals(name)) {
            return new OneMinutePeriod();
        } else if (FIFTEEN_MINUTES_PERIOD.equals(name)) {
            return new FifteenMinutesPeriod();
        } else if (THIRTY_MINUTES_PERIOD.equals(name)) {
            return new ThirtyMinutesPeriod();
        } else if (ONE_HOUR_PERIOD.equals(name)) {
            return new OneHourPeriod();
        } else if (THREE_HOURS_PERIOD.equals(name)) {
            return new ThreeHoursPeriod();
        } else {
            Log.e(TAG, "unsupported period " + name);
            return null;
        }
    }
    
}
