package ru.opsb.myxa.android;

import static ru.opsb.myxa.android.Graphs.BUCKET_DIR;
import static ru.opsb.myxa.android.Graphs.BUCKET_NAME;
import static ru.opsb.myxa.android.Graphs.GRAPHS;
import static ru.opsb.myxa.android.Graphs.IMAGES_URI;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.opsb.myxa.android.Graphs.GraphInfo;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

/**
 *  Downloads temperature graphs and updates the graph images
 *  in the MediaStore.
 */
public class GraphsUpdater implements Runnable, Constants {

    /** Projection to select last modified date for the images */
    static final String[] LAST_MODIFIED_PROJECTION = {
        MediaStore.Images.ImageColumns._ID,
        MediaStore.Images.ImageColumns.DATE_TAKEN,
        MediaStore.Images.ImageColumns.DISPLAY_NAME,
    };
    /** Selection to select last modified date for the images */
    static final String GRAPHS_SELECTION = 
        MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + " = \"" + BUCKET_NAME + "\"";

    /** Context */
    Context context;
    /** Content resolver */
    ContentResolver contentResolver;
    /** Graphs information currently processed */
    List<GraphInfo> graphs;
    /** Graphs information currently processed by the graph name */
    Map<String, GraphInfo> graphsMap;
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
        contentResolver = context.getContentResolver();
        graphs = new ArrayList<GraphInfo>();
        graphsMap = new HashMap<String, GraphInfo>();
        for (int i = 0; i < GRAPHS.length; i++) {
            GraphInfo graphInfo = GRAPHS[i].copy();
            graphs.add(graphInfo);
            graphsMap.put(graphInfo.name, graphInfo);
        }
    }
    
    /**
     *  Updates all graphs.
     */
    public void run() {
        getLastModified();
        insertEmptyImages();
        for (GraphInfo graph : graphs) {
            try {
                updateGraph(graph);
            } catch (IOException e) {
                Log.w(TAG, "failed to update graph", e);
                sendError(handler, e);
                return;
            }
        }
        sendSuccess(handler);
    }
    
    /**
     *  Update one graph.
     *  @throws IOException if some error occurred
     */
    void updateGraph(GraphInfo graphInfo) throws IOException {
        HttpLoader loader = new HttpLoader(graphInfo.url, graphInfo.lastModified);
        loader.load();
        graphInfo.lastModified = loader.getLastModified();
        byte[] content = loader.getContent();
        if (loader.isModified() && content != null) {
            saveGraph(graphInfo, content);
        }
    }
    
    /**
     *  Gets the previously saved last modified time for the graph.
     *  @param  graphInfos   lastModified property of this objects is updated
     */
    void getLastModified() {
        Cursor cursor = contentResolver.query(IMAGES_URI,
                LAST_MODIFIED_PROJECTION, GRAPHS_SELECTION, null, null);
        //Cursor cursor = contentResolver.query(IMAGES_URI,
        //        null, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {

            int idColumn = cursor.getColumnIndex(
                    MediaStore.Images.ImageColumns._ID);
            int nameColumn = cursor.getColumnIndex(
                    MediaStore.Images.ImageColumns.DISPLAY_NAME);
            int lastModifiedColumn = cursor.getColumnIndex(
                    MediaStore.Images.ImageColumns.DATE_TAKEN);
        
            do {
                //Log.d(TAG, "image: " + 
                //        DatabaseUtils.dumpCurrentRowToString(cursor));
                
                String name = cursor.getString(nameColumn);
                GraphInfo graphInfo = graphsMap.get(name);
                if (graphInfo == null) {
                    continue;
                }
                graphInfo.id = cursor.getLong(idColumn);
                graphInfo.lastModified = cursor.getLong(lastModifiedColumn);
                Log.d(TAG, "found image " + name + " id = " + graphInfo.id);
            } while (cursor.moveToNext());

            cursor.close();
        }
    }
    
    /**
     *  Inserts empty images if the lastModified is empty.
     *  @param  graphInfo   lastModified property of this object is updated
     */
    void insertEmptyImages() {
        for (GraphInfo graphInfo : graphs) {
            if (graphInfo.id >= 0) {
                continue;
            }

            if (emptyGraph == null) {
                InputStream in = context.getResources().openRawResource(R.drawable.empty_graph);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try {
                    int b = in.read();
                    while (b >= 0) {
                        out.write(b);
                        b = in.read();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "failed to load empty graph drawable from resources", e);
                    continue;
                }
                emptyGraph = out.toByteArray();
            }
            
            saveGraph(graphInfo, emptyGraph);
        }
    }
    
    /**
     *  Saves the graph to the content provider.
     *  @param  graphInfo   contains lastModified value
     *  @param  content     graph image got from HTTP server
     */
    void saveGraph(GraphInfo graphInfo, byte[] content) {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.w(TAG, "SD card is not mounted");
            return;
        }
        Log.d(TAG, "saving " + graphInfo.name);
        
        if (!BUCKET_DIR.isDirectory()) {
            BUCKET_DIR.mkdirs();
        }
        
        //update for MediaStore.Images means adding new and deleting old image
        if (graphInfo.id >= 0) {
            deleteDatabase(graphInfo);
        }
        
        try { 
            OutputStream out = new FileOutputStream(graphInfo.path); 
            out.write(content); 
            out.close(); 
        } catch (Exception e) { 
            Log.e(TAG, "exception while writing image", e);
            sendError(handler, e);
        }
        
        insertDatabase(graphInfo);
    }
    
    void deleteDatabase(GraphInfo graphInfo) {
        Uri uri = ContentUris.withAppendedId(IMAGES_URI, graphInfo.id);
        int rowsDeleted = contentResolver.delete(uri, null, null);
        Log.d(TAG, "deleted " + rowsDeleted + " rows with id = " + graphInfo.id);
    }
    
    void insertDatabase(GraphInfo graphInfo) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, 
                graphInfo.name);
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE, 
                graphInfo.mimeType);
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN,
                graphInfo.lastModified);
        values.put(MediaStore.Images.ImageColumns.DATA, 
                graphInfo.path.toString());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, 
                BUCKET_NAME);
        //values.put(MediaStore.Images.ImageColumns.BUCKET_ID,
        //        BUCKET_ID);
        
        Uri uri = contentResolver.insert(IMAGES_URI, values);
        graphInfo.id = ContentUris.parseId(uri);
        Log.d(TAG, "inserted " + uri);
    }
    
    /**
     *  Sends result to the handler.
     */
    static void sendSuccess(Handler handler) {
        Message message = handler.obtainMessage(GRAPHS_UPDATE);
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
