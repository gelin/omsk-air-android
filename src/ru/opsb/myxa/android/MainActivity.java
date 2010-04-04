package ru.opsb.myxa.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.Toast;

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
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        storage = new PreferencesStorage(
                getSharedPreferences(PREFERENCES ,MODE_PRIVATE));
        
        /*
        Cursor cursor = managedQuery(Graphs.IMAGES_URI,
                null, GraphsUpdater.GRAPHS_SELECTION, null, null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_gallery_item, cursor,
                new String[] {MediaStore.Images.ImageColumns.DISPLAY_NAME},
                new int[] { android.R.id.text1 });
        */
        Gallery gallery = (Gallery)findViewById(R.id.graphs);
        gallery.setAdapter(new GraphImageAdapter(this));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTemperatureViews(storage.get());
        startUpdate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        case R.id.menu_preferences:
            startActivity(new Intent(this, Preferences.class));
        }
        return false;
    }

    /**
     *  Handles temperature updates.
     */
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case TEMPERATURE_UPDATE:
                Bundle values = msg.getData();
                updateTemperatureViews(values);
                setProgressBarIndeterminateVisibility(false);
                updateAll();
                updateGraphs();
                break;
            case ERROR:
                showError(String.valueOf(msg.obj));
                setProgressBarIndeterminateVisibility(false);
                break;
            }
        }
    };

    void startUpdate() {
        setProgressBarIndeterminateVisibility(true);
        Thread updater = new Thread(new TemperatureUpdater(handler,
                getSharedPreferences(PREFERENCES ,MODE_PRIVATE)));
        updater.start();
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
        } else if (Math.abs(temperature) <= 0.1f) {
            return getResources().getString(R.string.zero_temp);
        }
        return getResources().getString(R.string.temp_format, temperature);
    }

    void showError(int resString, String error) {
        String message = getResources().getString(resString, error);
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
    }
    
    void showError(String error) {
        showError(R.string.update_error, error);
    }

    void updateAll() {
        startService(UpdateService.UPDATE_ALL_INTENT);
    }
    
    /**
     *  Handles graph updates.
     */
    final Handler graphHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case GRAPHS_UPDATE:
                //update graph views
                setProgressBarIndeterminateVisibility(false);
                break;
            case ERROR:
                showError(R.string.update_graphs_error, String.valueOf(msg.obj));
                setProgressBarIndeterminateVisibility(false);
                break;
            }
        }
    };
    
    void updateGraphs() {
        setProgressBarIndeterminateVisibility(true);
        Thread updater = new Thread(new GraphsUpdater(this, graphHandler));
        updater.start();
    }

}