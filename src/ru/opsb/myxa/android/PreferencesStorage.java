package ru.opsb.myxa.android;

import java.util.Map;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

/**
 *  Stores and retrieves Bundle to/from the SharedPreferences.
 *  @author gelin
 */
public class PreferencesStorage {

    /** Preferences instance to save Bundle */
    SharedPreferences preferences;
    
    /**
     *  Creates the storage for the preferences.
     */
    public PreferencesStorage(SharedPreferences preferences) {
        this.preferences = preferences;
    }
    
    /**
     *  Saves the Bundle values to the SharedPreferences
     */
    public void put(Bundle bundle) {
        SharedPreferences.Editor edit = preferences.edit();
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (value instanceof String) {
                edit.putString(key, (String)value);
            } else if (value instanceof Boolean) {
                edit.putBoolean(key, (Boolean)value);
            } else if (value instanceof Integer) {
                edit.putInt(key, (Integer)value);
            } else if (value instanceof Long) {
                edit.putLong(key, (Long)value);
            } else if (value instanceof Float) {
                edit.putFloat(key, (Float)value);
            } else {
                Log.w(this.getClass().getName(), "unsupported type: " + value.getClass());
            }
        }
        edit.commit();
    }
    
    /**
     *  Gets values from the SharedPreferences as a Bundle. 
     */
    public Bundle get() {
        Map<String, ?> values = preferences.getAll();
        Bundle result = new Bundle(values.size());
        for (String key : values.keySet()) {
            Object value = values.get(key);
            if (value instanceof String) {
                result.putString(key, (String)value);
            } else if (value instanceof Boolean) {
                result.putBoolean(key, (Boolean)value);
            } else if (value instanceof Integer) {
                result.putInt(key, (Integer)value);
            } else if (value instanceof Long) {
                result.putLong(key, (Long)value);
            } else if (value instanceof Float) {
                result.putFloat(key, (Float)value);
            } else {
                Log.w(this.getClass().getName(), "unsupported type: " + value.getClass());
            }
        }
        return result;
    }
    
}
