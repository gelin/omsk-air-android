package ru.opsb.myxa.android.periods;

import java.util.Calendar;

public abstract class AbstractPeriod implements Period {

    /** "now" calendar */
    protected Calendar calendar = Calendar.getInstance();
    
    /**
     *  Returns copy of the "now" calendar.
     */
    protected Calendar getNow() {
        return (Calendar)calendar.clone();
    }

}
