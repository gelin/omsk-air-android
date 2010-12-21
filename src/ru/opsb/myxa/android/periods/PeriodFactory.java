package ru.opsb.myxa.android.periods;

import ru.opsb.myxa.android.Constants;

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
            //return new OneMinutePeriod();
            return new SimplePeriod(60 * 1000); //60 seconds
        } else if (FIFTEEN_MINUTES_PERIOD.equals(name)) {
            //return new FifteenMinutesPeriod();
            return new SimplePeriod(15 * 60 * 1000);
        } else if (THIRTY_MINUTES_PERIOD.equals(name)) {
            //return new ThirtyMinutesPeriod();
            return new SimplePeriod(30 * 60 * 1000);
        } else if (ONE_HOUR_PERIOD.equals(name)) {
            //return new OneHourPeriod();
            return new SimplePeriod(60 * 60 * 1000);    //60 minutes
        } else if (THREE_HOURS_PERIOD.equals(name)) {
            //return new ThreeHoursPeriod();
            return new SimplePeriod(3 * 60 * 60 * 1000);
        } else {
            return new NeverPeriod();
        }
    }
    
}
