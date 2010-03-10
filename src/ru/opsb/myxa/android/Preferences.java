package ru.opsb.myxa.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 *  Activity to display application preferences.
 */
public class Preferences extends PreferenceActivity
        implements OnSharedPreferenceChangeListener {

    /** Shared preferences which saves configuration options
     *  displayed by this activity. */
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (prefs == null) {
            return;
        }
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (prefs == null) {
            return;
        }
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(
            SharedPreferences prefs, String name) {
        startService(UpdateService.UPDATE_ALL_INTENT);
    }

}
