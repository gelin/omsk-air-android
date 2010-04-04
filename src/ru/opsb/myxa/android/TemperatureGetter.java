package ru.opsb.myxa.android;

import java.io.IOException;
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
        
        HttpLoader loader = new HttpLoader(
                URL, prevValues.getLong(LAST_MODIFIED, 0));
        loader.load();

        result.putLong(LAST_MODIFIED, loader.getLastModified());

        byte[] content = loader.getContent();
        
        if (!loader.isModified() || content == null) {
            if (prevValues.containsKey(TEMPERATURE)) {
                result.putFloat(TEMPERATURE,
                        prevValues.getFloat(TEMPERATURE, Float.NaN));
            }
            return result;
        }

        String js = new String(content, "ISO-8859-1");
        result.putFloat(TEMPERATURE, parseTemperature(js));

        return result;
    }

    /**
     * 	Parses the temperature, encoded in JavaScript code.
     */
    float parseTemperature(String js) {
        //for test
        //return 1.0f;
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
