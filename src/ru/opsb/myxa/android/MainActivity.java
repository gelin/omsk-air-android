package ru.opsb.myxa.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

/**
 *  Main activity of the application.
 */
public class MainActivity extends Activity implements Constants {

    /** Storage for the previous temperature values */
    PreferencesStorage storage;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        storage = new PreferencesStorage(getPreferences(MODE_PRIVATE));
        updateTemperatureViews(storage.get());
    }

    /**	Called when the activity becomes active. */
    @Override
    public void onResume() {
        super.onResume();
        TemperatureGetter getter = 
                new TemperatureGetter(handler, storage.get());
        getter.start();
    }
    
    /**
     *  Handles temperature updates.
     */
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what != TEMPERATURE_UPDATE) {
                return;
            }
            Bundle values = msg.getData();
            updateTemperatureViews(values);
            storage.put(values);
        }
    };
    
    void updateTemperatureViews(Bundle values) {
        float temp = values.getFloat(TEMPERATURE);
        TextView tempView = (TextView)findViewById(R.id.temp_value);
        tempView.setText(formatTemperature(temp));

        long lastModified = values.getLong(LAST_MODIFIED);
        TextView dateView = (TextView)findViewById(R.id.temp_date);
        dateView.setText(formatDate(lastModified));
    }
    
    String formatDate(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat(
                getResources().getString(R.string.date_format));
        return format.format(new Date(timestamp));
    }
    
    String formatTemperature(float temperature) {
        if (temperature == Float.NaN) {
            return getResources().getString(R.string.no_temp);
        }
        return getResources().getString(R.string.temp_format, temperature);
    }

}