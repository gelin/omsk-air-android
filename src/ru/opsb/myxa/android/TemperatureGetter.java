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
			float temp = getTemperature();
			Message msg = handler.obtainMessage();
			Bundle b = new Bundle();
			b.putFloat("temperature", temp);
			msg.setData(b);
			handler.sendMessage(msg);
		} catch (IOException e) {
			Log.e(this.getClass().getName(), "Failed to get temperature", e);
		}
	}
	
	/**
	 * 	Asks the temperature by the URL.
	 * 	@throws IOException 
	 */
	static float getTemperature() throws IOException {
		URL url = new URL(URL);
		URLConnection connection = url.openConnection();
		connection.connect();
		byte[] buf = new byte[connection.getContentLength()];
		int read = 0;
		InputStream stream = connection.getInputStream();
		do {
			read += stream.read(buf);
		} while (read < buf.length);
		String js = new String(buf, "ISO-8859-1");
		return parseTemperature(js);
	}
	
	/**
	 * 	Parses the temperature, encoded in JavaScript code.
	 */
	static float parseTemperature(String js) {
		Matcher m = JS_PATTERN.matcher(js);
		if (!m.find()) {
			Log.w(TemperatureGetter.class.getName(), "Failed to parse: " + js);
			return 0;
		}
		try {
			return Float.parseFloat(m.group(JS_GROUP));
		} catch (NumberFormatException e) {
			Log.w(TemperatureGetter.class.getName(), "Failed to parse: " + js);
			return 0;
		}
	}
	
}
