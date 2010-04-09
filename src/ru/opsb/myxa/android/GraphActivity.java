package ru.opsb.myxa.android;

import static ru.opsb.myxa.android.Graphs.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.opsb.myxa.android.Graphs.GraphInfo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 *  Main activity of the application.
 */
public class GraphActivity extends Activity implements Constants {

    /** Name of the Extra with graph index */
    public static final String EXTRA_GRAPH_INDEX = "graph_index";
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph);
        
        GraphInfo graphInfo = GRAPHS[
                getIntent().getIntExtra(EXTRA_GRAPH_INDEX, 0)];
        setTitle(graphInfo.title);
        updateGraphView(graphInfo);
        /*
        HorizontalScrollView scroll = (HorizontalScrollView)
                findViewById(R.id.graph_scroll);
        scroll.fullScroll(View.FOCUS_RIGHT);
        */
    }
    
    void updateGraphView(final GraphInfo graphInfo) {
        ImageView image = (ImageView)findViewById(R.id.graph_image);
        Bitmap bitmap = getBitmap(this, graphInfo);
        image.setImageBitmap(bitmap);
        image.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
    


}