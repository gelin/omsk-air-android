package ru.opsb.myxa.android.graphs;

import static ru.opsb.myxa.android.graphs.Graphs.BUCKET_DIR;
import static ru.opsb.myxa.android.graphs.Graphs.GRAPHS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import ru.opsb.myxa.android.Constants;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

/**
 *  Temporary storage to process graphs.
 *  {@link MainActivity} inits the storage and displays old graph images
 *  loaded from the disk, {@link GraphsUpdater} updates
 *  graph bitmaps and saves them to disk, {@link MainActivity} displays
 *  new bitmaps. 
 */
public class GraphsStorage implements Constants {

    /** Singleton instance */
    static GraphsStorage instance;
    
    /** Graphs information currently processed */
    List<Graph> graphs = new ArrayList<Graph>(Graphs.GRAPHS.length);
    
    /**
     *  Private constructor.
     */
    private GraphsStorage() {
    }
    
    /**
     *  Returns single instance of the storage.
     */
    public static synchronized GraphsStorage getInstance() {
        if (instance == null) {
            instance = new GraphsStorage();
        }
        return instance;
    }
    
    /**
     *  Initializes the storage. I.e. fill the storage with the graphs
     *  currently saved on disk.
     */
    public synchronized void init() {
        clear();
        for (int i = 0; i < GRAPHS.length; i++) {
            Graph graph = GRAPHS[i].copy();
            graph.load();
            graphs.add(graph);
        }
    }
    
    /**
     *  Returns the graph by index.
     */
    public synchronized Graph get(int index) {
        return graphs.get(index);
    }
    
    /**
     *  Returns the number of saved graphs.
     */
    public synchronized int size() {
        return graphs.size();
    }
    
    /**
     *  Clears all graph info saved in memory storage.
     */
    public synchronized void clear() {
        graphs.clear();
    }
    


}
