package ru.opsb.myxa.android;

import ru.opsb.myxa.android.graphs.Graph;
import ru.opsb.myxa.android.graphs.GraphsStorage;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

/**
 *  Main activity of the application.
 */
public class GraphActivity extends Activity implements Constants {

    /** Name of the Extra with graph index */
    public static final String EXTRA_GRAPH_INDEX = "graph_index";
    
    /** Graphs information currently processed */
    GraphsStorage graphs = GraphsStorage.getInstance();
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph);
        
        Graph graph = graphs.get(
                getIntent().getIntExtra(EXTRA_GRAPH_INDEX, 0));
        setTitle(graph.getTitle());
        updateGraphView(graph);
        /*
        HorizontalScrollView scroll = (HorizontalScrollView)
                findViewById(R.id.graph_scroll);
        scroll.fullScroll(View.FOCUS_RIGHT);
        */
    }
    
    void updateGraphView(final Graph graph) {
        ImageView image = (ImageView)findViewById(R.id.graph_image);
        Bitmap bitmap = null;
        synchronized (graphs) { 
            bitmap = graph.getBitmap();
        }
        if (bitmap == null) {
            image.setImageResource(R.drawable.empty_graph);
        } else {
            image.setImageBitmap(bitmap);
        }
        image.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
    


}