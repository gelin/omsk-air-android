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
            graphs.add(graph);
            readFromDisk(graph);
        }
    }
    
    /**
     *  Returns the graph by index.
     *  Operations over the graph should be synchronized!
     */
    public Graph get(int index) {
        return graphs.get(index);
    }
    
    /**
     *  Returns the number of saved graphs.
     */
    public int size() {
        return graphs.size();
    }
    
    /**
     *  Clears all graph info saved in memory storage.
     */
    public synchronized void clear() {
        graphs.clear();
    }
    
    
    /**
     *  Reads graph content from the disk.
     *  If the file is absent, the bitmap of the graph is set to null.
     */
    void readFromDisk(Graph graph) {
        File file = graph.getPath();
        if (!file.canRead()) {
            graph.setLastModified(0);
            graph.setBitmap(null);
            return;
        }
        graph.setLastModified(file.lastModified());
        graph.setBitmap(BitmapFactory.decodeFile(file.toString()));
    }
    
    /**
     *  Saves the graph to disk.
     *  @param  graph   contains lastModified value and file path
     *  @param  content graph image got from HTTP server
     *  @throws IOException if the graph cannot be saved
     */
    public static void saveGraph(Graph graph, byte[] content) throws IOException {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.w(TAG, "SD card is not mounted");
            return;
        }
        Log.d(TAG, "saving " + graph.getName());
        
        if (!BUCKET_DIR.isDirectory()) {
            BUCKET_DIR.mkdirs();
        }
        
        File file = graph.getPath();
        OutputStream out = new FileOutputStream(file); 
        out.write(content); 
        out.close();
        file.setLastModified(graph.getLastModified());
    }

}
