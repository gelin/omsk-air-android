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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

/**
 *  Main activity of the application.
 */
public class MainActivity extends Activity implements Constants {

    /** Storage for the previous temperature values */
    PreferencesStorage storage;
    
    /** Link to service. */
    UpdateServiceInterface service;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        storage = new PreferencesStorage(
                getSharedPreferences(PREFERENCES ,MODE_PRIVATE));
        updateTemperatureViews(storage.get());
        bindService(new Intent(UpdateServiceInterface.class.getName()),
                connection, BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        startUpdate();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        stopUpdate();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        if (LOCATE_INTENT.resolveActivity(getPackageManager()) == null) {
            menu.findItem(R.id.menu_locate).setVisible(false);
        }
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_refresh:
            startUpdate();
            return true;
        case R.id.menu_locate:
            startActivity(LOCATE_INTENT);
            return true;
        }
        return false;
    }
    
    
    /**
     *  Handles connection with the service.
     */
    final ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            service = UpdateServiceInterface.Stub.asInterface(binder);
            startUpdate();
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
            setProgressBarIndeterminateVisibility(false);
        }
        public void onError(String error) {
            showError(error);
            setProgressBarIndeterminateVisibility(false);
        }
    };
    
    void startUpdate() {
        setProgressBarIndeterminateVisibility(true);
        if (service != null) {
            try {
                service.registerCallback(callback);
                service.startUpdate();
            } catch (RemoteException e) {
                Log.w(TAG, "failed to start update", e);
            }
        }
    }
    
    void stopUpdate() {
        if (service != null) {
            try {
                service.unregisterCallback(callback);
            } catch (RemoteException e) {
                Log.w(TAG, "failed to unregister callback", e);
            }
        }
    }
    
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
    
    void showError(String error) {
        String message = getResources().getString(R.string.update_error, error);
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
    }

}