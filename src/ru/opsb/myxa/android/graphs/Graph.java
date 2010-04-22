/**
 * 
 */
package ru.opsb.myxa.android.graphs;

import static ru.opsb.myxa.android.graphs.Graphs.BUCKET_DIR;
import static ru.opsb.myxa.android.Constants.TAG;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

/**
 *  Contains all information about one graph.
 */
public class Graph {
    
    /** Graph index in storage */
    private int index;
    
    /** Graph URL to download image */
    private String url;
    
    /** Graph file path */
    private File path;
    
    /** Graph name */
    private String name;
    
    /** Resource ID of graph title */
    private int title;
    
    /** Resource ID of graph view */
    private int view;
    
    /** Graph last modified timestamp */
    private long lastModified;
    
    /** "Weight" of the graph pixel - number of milliseconds
     *  represented by the graph pixel - graph expiration time */
    private long expiration;
    
    /** Bitmap content of the graph */
    private Bitmap bitmap;
    
    /**
     *  Creates the graph with specified parameters 
     */
    Graph(int index, String url, String name, 
            int title, int view, long expiration) {
        this.index = index;
        this.url = url;
        this.name = name;
        this.title = title;
        this.view = view;
        this.expiration = expiration;
        this.path = new File(Graphs.BUCKET_DIR, name);
    }
    
    /**
     *  Creates a deep copy of the graph.
     *  @return new object with the same properties
     */
    public Graph copy() {
        Graph result = new Graph(
                this.index, this.url, this.name, 
                this.title, this.view, this.expiration);
        result.path = this.path;
        result.lastModified = this.lastModified;
        return result;
    }
    
    /**
     *  Returns graph index in the storage.
     */
    public int getIndex() {
        return index;
    }
    
    /**
     *  Returns graph URL to download from
     */
    public String getUrl() {
        return url;
    }
    
    /**
     *  Returns graph name.
     */
    public String getName() {
        return name;
    }
    
    /**
     *  Returns resource ID of the graph title.
     * @return
     */
    public int getTitle() {
        return title;
    }
    
    /**
     *  Returns resource ID of the graph view on the main activity.
     */
    public int getView() {
        return view;
    }
    
    /**
     *  Returns the graph expiration time in milliseconds.
     */
    public long getExpiration() {
        return expiration;
    }
    
    /**
     *  Returns the last modification timestamp.
     */
    public synchronized long getLastModified() {
        return lastModified;
    }
    
    /**
     *  Returns bitmap of the graph. Can return null.
     */
    public synchronized Bitmap getBitmap() {
        return bitmap;
    }
    
    /**
     *  Reads graph content from the disk.
     *  Sets bitmap and lastModified properties of the graph.
     *  If the file is absent, the bitmap of the graph is set to null.
     */
    synchronized void load() {
        File file = this.path;
        if (!file.canRead()) {
            this.lastModified = 0;
            this.bitmap = null;
            return;
        }
        this.lastModified = file.lastModified();
        this.bitmap = BitmapFactory.decodeFile(file.toString());
    }
    
    /**
     *  Saves the graph to disk and updates bitmap property.
     *  If the content of the graph is not a valid image,
     *  the bitmap property is set to null, lastModified is set
     *  to zero and the file on the disk is not updated.
     *  If CD card is not mounted, no file is saved, but bitmap is updated.
     *  @param  content content of the graph image downloaded from HTTP server
     *  @param  lastModified    Last-Modified HTTP header value
     *  @throws IOException if the graph cannot be saved
     */
    public synchronized void save(byte[] content, long lastModified) throws IOException {
        this.bitmap = BitmapFactory.decodeByteArray(content, 0, content.length);
        this.lastModified = lastModified;
        
        if (this.bitmap == null) {
            this.lastModified = 0;
            return;
        }
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.w(TAG, "SD card is not mounted");
            return;
        }
        
        Log.d(TAG, "saving " + this.name);
        
        if (!BUCKET_DIR.isDirectory()) {
            BUCKET_DIR.mkdirs();
        }
        
        File file = this.path;
        OutputStream out = new FileOutputStream(file); 
        out.write(content);
        out.close();
        file.setLastModified(this.lastModified);
    }
    
}