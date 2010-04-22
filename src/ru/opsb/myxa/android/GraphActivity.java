package ru.opsb.myxa.android;

import ru.opsb.myxa.android.graphs.Graph;
import ru.opsb.myxa.android.graphs.Graphs;
import ru.opsb.myxa.android.graphs.GraphsStorage;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;

/**
 *  Main activity of the application.
 */
public class GraphActivity extends Activity implements Constants {

    /** Name of the Extra with graph index */
    public static final String EXTRA_GRAPH_INDEX = "graph_index";
    
    /** Graph to display */
    Graph graph;
    
    /** Current display width */
    int displayWidth;
    /** Current display height */
    int displayHeight;
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph);
        
        GraphsStorage graphs = GraphsStorage.getInstance();
        graph = graphs.get(getIntent().getIntExtra(EXTRA_GRAPH_INDEX, 0));
        setTitle(graph.getTitle());
        getDisplaySize();
        updateGraphView();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        getDisplaySize();
        updateGraphView();
    }
    
    void updateGraphView() {
        ImageView image = (ImageView)findViewById(R.id.graph_image);
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
        if (displayWidth > bitmapWidth) {
            //scale up
            image.setAdjustViewBounds(false);
            int height = Math.min(displayHeight, bitmapHeight * 2);
            image.setMinimumHeight(height);
            image.setMinimumWidth((int)((float)bitmapWidth * height / bitmapHeight));
        } else {
            //use auto 1:1 scale
            image.setAdjustViewBounds(true);
        }
        
        image.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    void getDisplaySize() {
        WindowManager windowManager = getWindowManager(); 
        Display display = windowManager.getDefaultDisplay();
        displayWidth = display.getWidth();
        displayHeight = display.getHeight();
    }


}