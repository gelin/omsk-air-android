package ru.opsb.myxa.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TemperatureGetter extends Thread {

    /** Result bundle key with the temperature value. */
    public static final String TEMPERATURE = "temperature";
    /** Result bundle key with the last modified value. */
    public static final String LAST_MODIFIED = "last_modified";
    
    /**	URL of the temperature source */
    static final String URL = "http://myxa.opsb.ru/files/weather.js";

    /** Regexp to parse JS */
    static final Pattern JS_PATTERN = Pattern.compile("Therm\\s*=\\s*['\"](-?\\d+(\\.\\d+)?)['\"]");
    /**	Group number with temperature result */
    static final int JS_GROUP = 1;

    /**	Hander which receives the temperature */
    Handler handler;

    /**
     * 	Constructs temperature getter for specified handler.
     */
    public TemperatureGetter(Handler handler) {
        this.handler = handler;
    }

    /**
     * 	Runs the temperature getter.
     */
    public void run() {
        try {
            Bundle result = getTemperatureBundle();
            Message msg = handler.obtainMessage();
            msg.setData(result);
            handler.sendMessage(msg);
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "Failed to get temperature", e);
        }
    }

    /**
     * 	Asks the temperature by the URL.
     * 	@throws IOException 
     */
    static Bundle getTemperatureBundle() throws IOException {
        Bundle result = new Bundle();
        URL url = new URL(URL);
        URLConnection connection = url.openConnection();
        connection.connect();
        result.putLong(LAST_MODIFIED, connection.getLastModified());
        byte[] buf = new byte[connection.getContentLength()];
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

}
