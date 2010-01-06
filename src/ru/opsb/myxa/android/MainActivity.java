package ru.opsb.myxa.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class MainActivity extends Activity {

    /**
     * 	Handler which handles temperature value.
     */
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Float temp = msg.getData().getFloat(
                    TemperatureGetter.TEMPERATURE);
            TextView tempView = (TextView)findViewById(R.id.temp_value);
            tempView.setText(formatTemperature(temp));
            long lastModified = msg.getData().getLong(
                    TemperatureGetter.LAST_MODIFIED);
            TextView dateView = (TextView)findViewById(R.id.temp_date);
            dateView.setText(formatDate(lastModified));
        }
    };	

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    /**	Called when the activity becomes active. */
    @Override
    public void onResume() {
        super.onResume();
        TemperatureGetter getter = new TemperatureGetter(handler);
        getter.start();
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