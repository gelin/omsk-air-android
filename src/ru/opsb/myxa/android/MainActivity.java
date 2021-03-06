package ru.opsb.myxa.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.opsb.myxa.android.graphs.Graph;
import ru.opsb.myxa.android.graphs.Graphs;
import ru.opsb.myxa.android.graphs.GraphsStorage;
import ru.opsb.myxa.android.graphs.GraphsUpdater;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
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
 *  In onCreate() loads previously saved temperature values from shared preferences and
 *  graphs from disk files and displays them.
 *  In onResume() starts temperature values update. When message with new temperature
 *  comes, displays it and starts graphs update. When new graphs downloaded,
 *  displays them. When any update error occurs displays error message.
 */
public class MainActivity extends Activity implements Constants {

    /** Storage for the previous temperature values */
    PreferencesStorage storage;
    
    /** Graphs information currently processed */
    GraphsStorage graphs = GraphsStorage.getInstance();
    
    /** Current display width */
    int displayWidth;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        storage = new PreferencesStorage(
                getSharedPreferences(PREFERENCES ,MODE_PRIVATE));
        graphs.init();
        getDisplayWidth();
        updateAllGraphViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkSDCard();
        updateTemperatureViews(storage.get());
        getDisplayWidth();
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
                updateGraphView(graphs.get(msg.arg1));
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
        for (int i = 0; i < graphs.size(); i++) {
            updateGraphView(graphs.get(i));
        }
    }
    
    void updateGraphView(final Graph graph) {
        ImageView image = (ImageView)findViewById(graph.getView());
        Bitmap bitmap = graph.getBitmap();
        int bitmapWidth;
        int bitmapHeight;
        if (bitmap == null) {
            image.setImageResource(R.drawable.empty_graph);
            bitmapWidth = Graphs.WIDTH;
            bitmapHeight = Graphs.HEIGHT;
        } else {
            image.setImageBitmap(bitmap);
            bitmapWidth = bitmap.getWidth();
            bitmapHeight = bitmap.getHeight();
        }
        //scale the image by changing its height to fit the display width
        if (bitmapWidth < displayWidth) {
            //scale up
            image.setAdjustViewBounds(false);
            image.setMinimumHeight((int)((float)bitmapHeight * displayWidth / bitmapWidth));
        } else {
            //auto scale down
            image.setAdjustViewBounds(true);
        }
        
        image.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GraphActivity.class);
                intent.putExtra(GraphActivity.EXTRA_GRAPH_INDEX, graph.getIndex());
                startActivity(intent);
            }
        });
    }
    
    void checkSDCard() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            this.findViewById(R.id.graphs).setVisibility(View.VISIBLE);
            this.findViewById(R.id.sd_warn).setVisibility(View.GONE);
        } else {
            this.findViewById(R.id.graphs).setVisibility(View.GONE);
            this.findViewById(R.id.sd_warn).setVisibility(View.VISIBLE);
        }
    }
    
    void getDisplayWidth() {
        WindowManager windowManager = getWindowManager(); 
        Display display = windowManager.getDefaultDisplay();
        displayWidth = display.getWidth();
    }

}