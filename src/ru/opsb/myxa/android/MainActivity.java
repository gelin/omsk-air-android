package ru.opsb.myxa.android;

import static ru.opsb.myxa.android.Graphs.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.opsb.myxa.android.Graphs.GraphInfo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 *  Main activity of the application.
 */
public class MainActivity extends Activity implements Constants {

    /** Storage for the previous temperature values */
    PreferencesStorage storage;
    /** Minimum graph width */
    int minGraphWidth = 575;    //the width of images from myxa.opsb.ru

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        storage = new PreferencesStorage(
                getSharedPreferences(PREFERENCES ,MODE_PRIVATE));

        WindowManager windowManager = getWindowManager(); 
        Display display = windowManager.getDefaultDisplay();
        minGraphWidth = Math.max(display.getWidth(), display.getHeight());
        updateAllGraphViews();
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
                updateGraphView(GRAPHS[msg.arg1]);
                break;
            case UPDATE_COMPLETE:
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

    void updateAllGraphViews() {
        for (GraphInfo graphInfo : GRAPHS) {
            updateGraphView(graphInfo);
        }
    }
    
    void updateGraphView(final GraphInfo graphInfo) {
        ImageView image = (ImageView)findViewById(graphInfo.view);
        Bitmap bitmap = getBitmap(this, graphInfo);
        image.setImageBitmap(bitmap);
        image.setMinimumWidth(minGraphWidth);
        image.setMinimumHeight(0);
        
        image.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GraphActivity.class);
                intent.putExtra(GraphActivity.EXTRA_GRAPH_INDEX, graphInfo.index);
                startActivity(intent);
            }
        });
    }

}