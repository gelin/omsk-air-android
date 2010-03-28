package ru.opsb.myxa.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *  Loads content from remote HTTP resource.
 */
public class HttpLoader {
    
    /** Max content length, if we get more IOException will be thrown */
    static final int MAX_CONTENT_LENGTH = 50 * 1024;    //50kbytes
    
    String url;
    long lastModified;
    boolean isModified;
    byte[] content;
    
    /**
     *  Creates loader which will load the specified url.
     *  @param url URL to load
     *  @param lastModified milliseconds since epoch when the content was last modified
     */
    public HttpLoader(String url, long lastModified) {
        this.url = url;
        this.lastModified = lastModified;
    }
    
    /**
     *  Starts loading.
     *  @throws IOExection if loading fails
     */
    public void load() throws IOException {
        URL url = new URL(this.url);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setIfModifiedSince(lastModified);
        connection.connect();

        lastModified = connection.getLastModified();

        if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            isModified = false;
            return;
        }

        isModified = true;
        
        int contentLength = connection.getContentLength();
        if (contentLength > MAX_CONTENT_LENGTH) {
            throw new IOException("too large content: " + contentLength + " bytes");
        }
        byte[] buf = new byte[contentLength];
        int read = 0;
        InputStream stream = connection.getInputStream();
        do {
            read += stream.read(buf, read, contentLength - read);
        } while (read < buf.length);
        
        content = buf;
    }
    
    /**
     *  Returns the Last-Modified header received from the HTTP server or
     *  the previous value, set in constructor, if HTTP connection failed
     *  @return milliseconds since epoch
     */
    public long getLastModified() {
        return lastModified;
    }
    
    /**
     *  Returns true if the content is modified, as it is reported by HTTP server.
     *  @return <code>true</code> if the content is modified
     */
    public boolean isModified() {
        return isModified;
    }
    
    /**
     *  Returns the content received from the HTTP server or 
     *  <code>null</code> if some error occurred.
     *  @return byte array or <code>null</code>
     */
    public byte[] getContent() {
        return content;
    }

}
