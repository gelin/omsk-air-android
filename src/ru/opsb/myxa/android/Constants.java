package ru.opsb.myxa.android;

public interface Constants {
    
    /** Name of the shared preferences used to save temperature values */
    public static final String PREFERENCES = "temperature";
    
    /** Bundle key with the temperature value. */
    public static final String TEMPERATURE = "temperature";
    /** Bundle key with the last modified value. */
    public static final String LAST_MODIFIED = "last_modified";
    
    /** Message.what = temperature update */
    public static final int TEMPERATURE_UPDATE = 0;
    /** Message.what = error */
    public static final int ERROR = 1;

}
