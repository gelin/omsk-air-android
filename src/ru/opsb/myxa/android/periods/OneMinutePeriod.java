package ru.opsb.myxa.android.periods;

import java.util.Calendar;

public class OneMinutePeriod extends AbstractPeriod {

    public long getNextStart(long lastStart) {
        Calendar calendar = getNow();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);   //this minute with zero seconds
        calendar.add(Calendar.MINUTE, 1);   //next minute
        return calendar.getTimeInMillis();
    }

    public boolean isExpired(long lastStart) {
        Calendar calendar = getNow();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);   //this minute with zero seconds
        return lastStart < calendar.getTimeInMillis();   //strongly less!
    }

}
