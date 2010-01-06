package ru.opsb.myxa.android;

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
            float temp = msg.getData().getFloat("temperature");
            TextView text = (TextView)findViewById(R.id.temperature);
            text.setText("Температура в Омске: " + temp);
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
    
}