package ru.opsb.myxa.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 *  The thread which asks the server for the current temperature value
 *  and updates the provided SharedPrererences with returned values.
 */
public class TemperatureGetter implements Runnable, Constants {

    /**	URL of the temperature source */
    static final String URL = "http://myxa.opsb.ru/files/weather.js";
    
    /** Max content length */
    static final int MAX_CONTENT_LENGTH = 50 * 1024;    //50kbytes

    /** Regexp to parse JS */
    static final Pattern JS_PATTERN = Pattern.compile("Therm\\s*=\\s*['\"](-?\\d+(\\.\\d+)?)['\"]");
    /**	Group number with temperature result */
    static final int JS_GROUP = 1;

    /** Handler where send a message with new values. */
    Handler handler;
    
    /** Previous values (from the storage) */
    Bundle prevValues;

    /**
     *  Constructs temperature getter for specified handler.
     *  @param  handler handler where send updated temperature
     *  @param  prevValues  previously saved values
     */
    public TemperatureGetter(Handler handler, Bundle prevValues) {
        this.handler = handler;
        this.prevValues = prevValues;
    }

    /**
     * 	Runs the temperature getter.
     */
    public void run() {
        Log.d(TAG, "update start");
        try {
            Bundle result = getTemperatureBundle(prevValues);
            sendResult(handler, result);
        } catch (Exception e) {
            Log.e(TAG, "failed to get temperature", e);
            sendError(handler, e);
        }
    }

    /**
     *  Asks the temperature by the URL.
     *  @throws IOException if the temperature cannot be retrieved  
     */
    Bundle getTemperatureBundle(Bundle prevValues) throws IOException {
        Bundle result = new Bundle();
        
        URL url = new URL(URL);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setIfModifiedSince(prevValues.getLong(LAST_MODIFIED, 0));
        connection.connect();
        
        result.putLong(LAST_MODIFIED, connection.getLastModified());
        
        if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            if (prevValues.containsKey(TEMPERATURE)) {
                result.putFloat(TEMPERATURE, 
                        prevValues.getFloat(TEMPERATURE, Float.NaN));
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
    float parseTemperature(String js) {
        //for test
        //return 0.0f;
        Matcher m = JS_PATTERN.matcher(js);
        if (!m.find()) {
            Log.w(TAG, "Failed to parse: " + js);
            return Float.NaN;
        }
        try {
            return Float.parseFloat(m.group(JS_GROUP));
        } catch (NumberFormatException e) {
            Log.w(TAG, "Failed to parse: " + js);
            return Float.NaN;
        }
    }
    
    /**
     *  Sends result to the handler.
     */
    static void sendResult(Handler handler, Bundle result) {
        Message message = handler.obtainMessage(TEMPERATURE_UPDATE);
        message.setData(result);
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
