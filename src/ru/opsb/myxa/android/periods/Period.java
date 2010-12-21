package ru.opsb.myxa.android.periods;

/**
 *  Period, such as minute, half an hour, hour etc.
 *  Period has start and end. Start is before now. End is after now.
 *  Some timestamp is expired if it before the start of the current period.
 */
public interface Period {
    
    /**
     *  Returns true if the specified timestamp is already expired.
     *  @param  lastStart   previous period start
     */
    public boolean isExpired(long lastStart);
    
    /**
     *  Returns next period start time since now.
     *  @param  lastStart   previous period start
     */
    public long getNextStart(long lastStart);

}
