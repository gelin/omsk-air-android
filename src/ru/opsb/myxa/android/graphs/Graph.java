/**
 * 
 */
package ru.opsb.myxa.android.graphs;

import java.io.File;

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
     *  Returns file where graph image is saved.
     */
    public File getPath() {
        return path;
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
    public long getLastModified() {
        return lastModified;
    }
    /**
     *  Sets the last modification timestamp.
     */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
    
}