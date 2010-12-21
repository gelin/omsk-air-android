package ru.opsb.myxa.android.periods;

import java.util.Calendar;

public class OneHourPeriod extends AbstractPeriod {

    public long getNextStart(long lastStart) {
        Calendar calendar = getNow();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);   //this hour with zero minutes
        calendar.add(Calendar.HOUR_OF_DAY, 1);  //next hour
        return calendar.getTimeInMillis();
    }

    public boolean isExpired(long lastStart) {
        Calendar calendar = getNow();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);    //this hour with zero minutes
        return lastStart < calendar.getTimeInMillis();   //strongly less!
    }

}
