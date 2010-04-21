package ru.opsb.myxa.android.graphs;

import java.io.File;

import ru.opsb.myxa.android.R;
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

}
