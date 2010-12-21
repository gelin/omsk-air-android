package ru.opsb.myxa.android.graphs;

import java.io.IOException;
import java.util.Date;
import ru.opsb.myxa.android.Constants;
import ru.opsb.myxa.android.HttpLoader;
import android.content.Context;
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
    GraphsStorage graphs = GraphsStorage.getInstance();
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
    void updateGraph(Graph graph) throws IOException {
        Date now = new Date();
        if ((now.getTime() - graph.getLastModified()) < graph.getExpiration()) {
            //not expired
            return;
        }
        HttpLoader loader = new HttpLoader(graph.getUrl(), graph.getLastModified());
        loader.load();
        byte[] content = loader.getContent();
        long lastModified = loader.getLastModified();
        if (loader.isModified() && content != null) {
            graph.save(content, lastModified);
            if (graph.getBitmap() == null) {
                Log.w(TAG, "invalid content received from " + graph.getUrl());
                loader.load();
                content = loader.getContent();
                lastModified = loader.getLastModified();
                if (loader.isModified() && content != null) {
                    graph.save(content, lastModified);
                    if (graph.getBitmap() == null) {
                        Log.w(TAG, "invalid content received again from " + graph.getUrl());
                    }
                }
            }
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
