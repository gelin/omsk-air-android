package ru.opsb.myxa.android;

import android.content.Intent;
import android.net.Uri;

public interface Constants {
    
    /** Tag for logging */
    final String TAG = "ru.opsb.myxa.android";
    
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
    
    /** Intent to open Omsk on the map. */
    public Intent LOCATE_INTENT = new Intent(Intent.ACTION_VIEW, 
            Uri.parse("geo:" +
                    "54.99297920300393,73.36321234703064" +
                    //these actually points to the building where 
                    //the temperature sensor is located
                    "?z=6"
                    ));
    
    /** Preferences key for notification */
    public static final String NOTIFICATION = "notification";
    /** Preferences key for refresh period */
    public static final String REFRESH = "refresh";

}
