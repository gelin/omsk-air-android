package ru.opsb.myxa.android.periods;

import java.util.Calendar;

public class ThreeHoursPeriod extends AbstractPeriod {

    static final int PERIOD = 3;
    
    public long getNextStart(long lastStart) {
        Calendar calendar = getNow();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        calendar.set(Calendar.HOUR_OF_DAY, hours - hours % PERIOD);
        calendar.add(Calendar.HOUR_OF_DAY, PERIOD);
        return calendar.getTimeInMillis();
    }

    public boolean isExpired(long lastStart) {
        Calendar calendar = getNow();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        calendar.set(Calendar.HOUR_OF_DAY, hours - hours % PERIOD);
        return lastStart < calendar.getTimeInMillis();   //strongly less!
    }

}
