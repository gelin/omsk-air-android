package ru.opsb.myxa.android;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

/**
 *  Downloads temperature graphs and updates the graph images
 *  in the MediaStore.
 */
public class GraphsUpdater implements Runnable, Constants {

    static class GraphInfo {
        public long id = -1;    //-1 means not existed
        public String url;
        public String name;
        public long lastModified;
        public GraphInfo(String url, String name) {
            this.url = url;
            this.name = name;
        }
        public GraphInfo copy() {
            GraphInfo result = new GraphInfo(this.url, this.name);
            result.lastModified = this.lastModified;
            return result;
        }
    }
    
    /** Daily graph */
    static final GraphInfo DAILY = new GraphInfo(
            "http://myxa.opsb.ru/pics/daily.png", "daily.png");
    /** Weekly graph */
    static final GraphInfo WEEKLY = new GraphInfo(
            "http://myxa.opsb.ru/pics/weekly.png", "weekly.png");
    /** Monthly graph */
    static final GraphInfo MONTHLY = new GraphInfo(
            "http://myxa.opsb.ru/pics/monthly.png", "monthly.png");
    /** Yearly graph */
    static final GraphInfo YEARLY = new GraphInfo(
            "http://myxa.opsb.ru/pics/annual.png", "annual.png");
    /** All graphs */
    static final GraphInfo[] GRAPHS = {DAILY, WEEKLY, MONTHLY, YEARLY};
    
    /** URI of content provider for images */
    static final Uri IMAGES_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    //TODO: this requires SD card existence, need to detect it
    /** ID of the images Bucket (Album) */
    static final String BUCKET_ID = TAG;
    /** Name of the Bucket */
    static final String BUCKET_NAME = "Omsk temperature graphs";    //how to localize?
    /** Projection to select last modified date for the images */
    static final String[] LAST_MODIFIED_PROJECTION = {
        MediaStore.Images.ImageColumns._ID,
        MediaStore.Images.ImageColumns.DATE_TAKEN,
        MediaStore.Images.ImageColumns.DISPLAY_NAME,
    };
    /** Selection to select last modified date for the images */
    static final String LAST_MODIFIED_SELECTION = 
        MediaStore.Images.ImageColumns.BUCKET_ID + " = \"" + BUCKET_ID + "\"";
    
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
    
    /**
     *  Creates graphs updater.
     *  @param context  application context
     */
    public GraphsUpdater(Context context) {
        this.context = context;
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
    }
    
    /**
     *  Update one graph.
     *  @throws IOException if some error iccured
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
                LAST_MODIFIED_PROJECTION, LAST_MODIFIED_SELECTION, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {

            int idColumn = cursor.getColumnIndex(
                    MediaStore.Images.ImageColumns._ID);
            int nameColumn = cursor.getColumnIndex(
                    MediaStore.Images.ImageColumns.DISPLAY_NAME);
            int lastModifiedColumn = cursor.getColumnIndex(
                    MediaStore.Images.ImageColumns.DATE_TAKEN);
        
            do {
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
            
            Log.d(TAG, "inserting " + graphInfo.name);
            
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, 
                    graphInfo.name);
            values.put(MediaStore.Images.ImageColumns.DATE_TAKEN,
                    graphInfo.lastModified);
            values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, 
                    BUCKET_NAME);
            values.put(MediaStore.Images.ImageColumns.BUCKET_ID,
                    BUCKET_ID);
            
            Uri uri = contentResolver.insert(IMAGES_URI, values);
            Log.d(TAG, "inserted " + uri);
            
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
            
            try { 
                OutputStream out = contentResolver.openOutputStream(uri); 
                out.write(emptyGraph); 
                out.close(); 
            } catch (Exception e) { 
                Log.e(TAG, "exception while writing image", e); 
            }
        }
    }
    
    /**
     *  Saves the graph to the content provider.
     *  @param  graphInfo   contains lastModified value
     *  @param  content     graph image got from HTTP server
     */
    void saveGraph(GraphInfo graphInfo, byte[] content) {
        //TODO
    }

}
