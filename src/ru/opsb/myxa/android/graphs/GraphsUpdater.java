package ru.opsb.myxa.android.graphs;

import static ru.opsb.myxa.android.graphs.Graphs.BUCKET_DIR;
import static ru.opsb.myxa.android.graphs.Graphs.GRAPHS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.opsb.myxa.android.Constants;
import ru.opsb.myxa.android.HttpLoader;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 *  Downloads temperature graphs and updates the graph images
 *  in the MediaStore.
 */
public class GraphsUpdater implements Runnable, Constants {

    /** Context */
    Context context;
    /** Graphs information currently processed */
    List<Graph> graphs;
    /** Empty graph image */
    byte[] emptyGraph;
    /** Handler to receive update results */
    Handler handler;
    
    /**
     *  Creates graphs updater.
     *  @param context  application context
     *  @param handler to receive update results
     */
    public GraphsUpdater(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        graphs = new ArrayList<Graph>();
        for (int i = 0; i < GRAPHS.length; i++) {
            Graph graph = GRAPHS[i].copy();
            graphs.add(graph);
        }
    }
    
    /**
     *  Updates all graphs.
     */
    public void run() {
        for (int i = 0; i < graphs.size(); i++) {
            Graph graph = graphs.get(i);
            try {
                updateGraph(graph);
                sendSuccess(handler, i);
            } catch (IOException e) {
                Log.w(TAG, "failed to update graph", e);
                sendError(handler, e);
                return;
            }
        }
        sendComplete(handler);
    }
    
    /**
     *  Update one graph.
     *  @throws IOException if some error occurred
     */
    void updateGraph(Graph graphInfo) throws IOException {
        getLastModified(graphInfo);
        Date now = new Date();
        if ((now.getTime() - graphInfo.getLastModified()) < graphInfo.getExpiration()) {
            //not expired
            return;
        }
        HttpLoader loader = new HttpLoader(graphInfo.getUrl(), graphInfo.getLastModified());
        loader.load();
        graphInfo.setLastModified(loader.getLastModified());
        byte[] content = loader.getContent();
        if (loader.isModified() && content != null) {
            saveGraph(graphInfo, content);
        }
    }
    
    /**
     *  Gets the previously saved last modified time for the graph.
     */
    void getLastModified(Graph graph) {
        File file = graph.getPath();
        if (!file.canRead()) {
            return;
        }
        graph.setLastModified(file.lastModified());
    }
    
    /**
     *  Saves the graph to the content provider.
     *  @param  graph   contains lastModified value
     *  @param  content     graph image got from HTTP server
     */
    void saveGraph(Graph graph, byte[] content) {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.w(TAG, "SD card is not mounted");
            return;
        }
        Log.d(TAG, "saving " + graph.getName());
        
        if (!BUCKET_DIR.isDirectory()) {
            BUCKET_DIR.mkdirs();
        }
        
        try {
            File file = graph.getPath();
            OutputStream out = new FileOutputStream(file); 
            out.write(content); 
            out.close();
            file.setLastModified(graph.getLastModified());
        } catch (Exception e) { 
            Log.e(TAG, "exception while writing image", e);
            sendError(handler, e);
        }
    }
    
    /**
     *  Sends result to the handler.
     */
    static void sendSuccess(Handler handler, int graphIndex) {
        Message message = handler.obtainMessage(GRAPHS_UPDATE);
        message.arg1 = graphIndex;
        handler.sendMessage(message);
    }
    
    /**
     *  Sends result to the handler.
     */
    static void sendComplete(Handler handler) {
        Message message = handler.obtainMessage(UPDATE_COMPLETE);
        handler.sendMessage(message);
    }

    /**
     *  Sends error to the handler.
     */
    static void sendError(Handler handler, Exception error) {
        Message message = handler.obtainMessage(ERROR);
        message.obj = error.getMessage();
        handler.sendMessage(message);
    }

}
