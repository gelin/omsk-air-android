package ru.opsb.myxa.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

/**
 *  Main activity of the application.
 */
public class MainActivity extends Activity 
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        onSharedPreferenceChanged(preferences, TemperatureGetter.TEMPERATURE);
        onSharedPreferenceChanged(preferences, TemperatureGetter.LAST_MODIFIED);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**	Called when the activity becomes active. */
    @Override
    public void onResume() {
        super.onResume();
        TemperatureGetter getter = 
                new TemperatureGetter(getPreferences(MODE_PRIVATE));
        getter.start();
    }
    
    /**
     *  Handler which updates view.
     */
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            if (preferences.contains(TemperatureGetter.TEMPERATURE)) {
                Float temp = preferences.getFloat(
                        TemperatureGetter.TEMPERATURE, 0);
                TextView tempView = (TextView)findViewById(R.id.temp_value);
                tempView.setText(formatTemperature(temp));
            }
            if (preferences.contains(TemperatureGetter.LAST_MODIFIED)) {
                long lastModified = preferences.getLong(
                        TemperatureGetter.LAST_MODIFIED, 0);
                TextView dateView = (TextView)findViewById(R.id.temp_date);
                dateView.setText(formatDate(lastModified));
            }
        }
    };
    
    /**
     *  Initiated view update on temperature changes.
     */
    public void onSharedPreferenceChanged(
            SharedPreferences preferences, String key) {
        //actually updates views
        handler.sendEmptyMessage(0);
    }
    
    String formatDate(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat(
                getResources().getString(R.string.date_format));
        return format.format(new Date(timestamp));
    }
    
    String formatTemperature(Float temperature) {
        if (temperature == null) {
            return getResources().getString(R.string.no_temp);
        }
        return getResources().getString(R.string.temp_format, temperature);
    }

}