package ru.opsb.myxa.android.periods;

import java.util.Calendar;

public class ThirtyMinutesPeriod extends AbstractPeriod {

    static final int PERIOD = 30;
    
    public long getNextStart(long lastStart) {
        Calendar calendar = getNow();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        int minutes = calendar.get(Calendar.MINUTE);
        calendar.set(Calendar.MINUTE, minutes - minutes % PERIOD);
        calendar.add(Calendar.MINUTE, PERIOD);
        return calendar.getTimeInMillis();
    }

    public boolean isExpired(long lastStart) {
        Calendar calendar = getNow();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        int minutes = calendar.get(Calendar.MINUTE);
        calendar.set(Calendar.MINUTE, minutes - minutes % PERIOD);
        return lastStart < calendar.getTimeInMillis();   //strongly less!
    }

}
