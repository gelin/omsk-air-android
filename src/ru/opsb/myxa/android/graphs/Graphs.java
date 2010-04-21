package ru.opsb.myxa.android.graphs;

import java.io.File;

import ru.opsb.myxa.android.R;
import ru.opsb.myxa.android.R.drawable;
import ru.opsb.myxa.android.R.id;
import ru.opsb.myxa.android.R.string;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

/**
 *  Contains the set of predefined graphs.
 */
public class Graphs {

    /** Name of the Bucket */
    static final String BUCKET_NAME = "omsk_temp";
    /** Bucket directory */
    static final File BUCKET_DIR = new File(
            Environment.getExternalStorageDirectory(), BUCKET_NAME);
    
    /** Daily graph */
    static final Graph DAILY = new Graph(0,
            "http://myxa.opsb.ru/pics/daily.png", "daily.png", 
            R.string.daily_graph, R.id.daily_graph,
            24 * 60 * 60 * 1000 / 500);
    /** Weekly graph */
    static final Graph WEEKLY = new Graph(1,
            "http://myxa.opsb.ru/pics/weekly.png", "weekly.png", 
            R.string.weekly_graph, R.id.weekly_graph,
            7 * 24 * 60 * 60 * 1000 / 500);
    /** Monthly graph */
    static final Graph MONTHLY = new Graph(2,
            "http://myxa.opsb.ru/pics/monthly.png", "monthly.png", 
            R.string.monthly_graph, R.id.monthly_graph,
            30 * 60 * 60 * 1000 / 500);
    /** Yearly graph */
    static final Graph YEARLY = new Graph(3,
            "http://myxa.opsb.ru/pics/annual.png", "annual.png",
            R.string.annual_graph, R.id.annual_graph,
            365 * 24 * 60 * 60 * 1000 / 500);
    
    /** All graphs */
    public static final Graph[] GRAPHS = {DAILY, WEEKLY, MONTHLY, YEARLY};
    
    /**
     *  Reads bitmap from the graph file.
     *  If the file is missed or corrupted, returns empty_graph from resources.
     */
    public static Bitmap getBitmap(Context context, Graph graph) {
        Bitmap bitmap = BitmapFactory.decodeFile(graph.getPath().toString());
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.empty_graph);
        }
        return bitmap;
    }

}
