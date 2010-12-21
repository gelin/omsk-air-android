package ru.opsb.myxa.android.periods;

/**
 *  This period doesn't try to start at zero minutes,
 *  it just adds the number of seconds to the last start time.
 */
public class SimplePeriod implements Period {

    long length;
    
    /**
     *  Creates the period.
     *  @param  length  period length in milliseconds
     */
    public SimplePeriod(long length) {
        this.length = length;
    }
    
    
    public long getNextStart(long lastStart) {
        return lastStart + length;
    }

    public boolean isExpired(long lastStart) {
        long now = System.currentTimeMillis();
        return now > lastStart + length;
    }

}
