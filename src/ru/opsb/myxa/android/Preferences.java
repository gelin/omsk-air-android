package ru.opsb.myxa.android;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 *  Activity to display application preferences.
 */
public class Preferences extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}
