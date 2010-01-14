package ru.opsb.myxa.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

/**
 *  Main activity of the application.
 */
public class MainActivity extends Activity implements Constants {

    /** Tag for logging */
    final String TAG = getClass().getName();
    
    /** Storage for the previous temperature values */
    PreferencesStorage storage;
    
    /** Link to service. */
    UpdateServiceInterface service;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        storage = new PreferencesStorage(
                getSharedPreferences(PREFERENCES ,MODE_PRIVATE));
        updateTemperatureViews(storage.get());
        bindService(new Intent(UpdateServiceInterface.class.getName()),
                connection, BIND_AUTO_CREATE);
    }

    /**	Called when the activity becomes active. */
    @Override
    public void onResume() {
        super.onResume();
        if (service != null) {
            try {
                service.startUpdate();
            } catch (RemoteException e) {
                Log.w(TAG, "failed to start update", e);
            }
        }
    }
    
    /**
     *  Handles connection with the service.
     */
    final ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            UpdateServiceInterface service = 
                    UpdateServiceInterface.Stub.asInterface(binder);
            try {
                service.registerCallback(callback);
                service.startUpdate();
            } catch (RemoteException e) {
                Log.e(TAG, "failed to register callback", e);
            }
        }
        public void onServiceDisconnected(ComponentName name) {
            service = null;
        }
    };
    
    /**
     *  Handles temperature updates.
     */
    final UpdateServiceCallback callback = new UpdateServiceCallback.Stub() {
        public void onTemperatureUpdate(Bundle values) throws RemoteException {
            updateTemperatureViews(values);
        }
    };
    
    void updateTemperatureViews(Bundle values) {
        float temp = values.getFloat(TEMPERATURE, Float.NaN);
        TextView tempView = (TextView)findViewById(R.id.temp_value);
        tempView.setText(formatTemperature(temp));

        long lastModified = values.getLong(LAST_MODIFIED, 0);
        TextView dateView = (TextView)findViewById(R.id.temp_date);
        dateView.setText(formatDate(lastModified));
    }
    
    String formatDate(long timestamp) {
        if (timestamp == 0) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat(
                getResources().getString(R.string.date_format));
        return format.format(new Date(timestamp));
    }
    
    String formatTemperature(float temperature) {
        if (Float.isNaN(temperature)) {
            return getResources().getString(R.string.no_temp);
        }
        return getResources().getString(R.string.temp_format, temperature);
    }

}