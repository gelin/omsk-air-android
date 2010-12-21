package ru.opsb.myxa.android.periods;

/**
 *  Period which never expires.
 */
public class NeverPeriod implements Period {

    /**
     *  Returns 0;
     */
    public long getNextStart(long lastStart) {
        return 0;
    }

    /**
     *  Returns false.
     */
    public boolean isExpired(long lastStart) {
        return false;
    }

}
