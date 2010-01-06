package ru.opsb.myxa.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

/**
 *  The thread which asks the server for the current temperature value
 *  and updates the provided SharedPrererences with returned values.
 */
public class TemperatureGetter extends Thread {

    /** Result bundle key with the temperature value. */
    public static final String TEMPERATURE = "temperature";
    /** Result bundle key with the last modified value. */
    public static final String LAST_MODIFIED = "last_modified";
    
    /**	URL of the temperature source */
    static final String URL = "http://myxa.opsb.ru/files/weather.js";
    
    /** Max content length */
    static final int MAX_CONTENT_LENGTH = 50 * 1024;    //50kbytes

    /** Regexp to parse JS */
    static final Pattern JS_PATTERN = Pattern.compile("Therm\\s*=\\s*['\"](-?\\d+(\\.\\d+)?)['\"]");
    /**	Group number with temperature result */
    static final int JS_GROUP = 1;

    /** Preferences which will be updated */
    SharedPreferences preferences;

    /**
     * 	Constructs temperature getter for specified handler.
     */
    public TemperatureGetter(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    /**
     * 	Runs the temperature getter.
     */
    public void run() {
        try {
            Bundle result = getTemperatureBundle();
            updatePreferences(result);
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "Failed to get temperature", e);
        }
    }

    /**
     * 	Asks the temperature by the URL.
     * 	@throws IOException 
     */
    Bundle getTemperatureBundle() throws IOException {
        Bundle result = new Bundle();
        
        URL url = new URL(URL);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setIfModifiedSince(preferences.getLong(LAST_MODIFIED, 0));
        connection.connect();
        
        result.putLong(LAST_MODIFIED, connection.getLastModified());
        
        if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            if (preferences.contains(TEMPERATURE)) {
                result.putFloat(TEMPERATURE, 
                        preferences.getFloat(TEMPERATURE, 0));
            }
            return result;
        }
        
        int contentLength = connection.getContentLength();
        if (contentLength > MAX_CONTENT_LENGTH) {
            throw new IOException("too large content: " + contentLength + " bytes");
        }
        byte[] buf = new byte[contentLength];
        int read = 0;
        InputStream stream = connection.getInputStream();
        do {
            read += stream.read(buf);
        } while (read < buf.length);
        String js = new String(buf, "ISO-8859-1");
        result.putFloat(TEMPERATURE, parseTemperature(js));
        
        return result;
    }

    /**
     * 	Parses the temperature, encoded in JavaScript code.
     */
    static Float parseTemperature(String js) {
        Matcher m = JS_PATTERN.matcher(js);
        if (!m.find()) {
            Log.w(TemperatureGetter.class.getName(), "Failed to parse: " + js);
            return null;
        }
        try {
            return Float.parseFloat(m.group(JS_GROUP));
        } catch (NumberFormatException e) {
            Log.w(TemperatureGetter.class.getName(), "Failed to parse: " + js);
            return null;
        }
    }
    
    /**
     *  Updates preferences with new values.
     */
    void updatePreferences(Bundle bundle) {
        SharedPreferences.Editor edit = preferences.edit();
        edit.putLong(LAST_MODIFIED, bundle.getLong(LAST_MODIFIED));
        edit.putFloat(TEMPERATURE, bundle.getFloat(TEMPERATURE));
        edit.commit();
    }

}
