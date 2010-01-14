package ru.opsb.myxa.android;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

public class UpdateService extends Service implements Constants {

    /** Tag for logging */
    final String TAG = getClass().getName();
    
    /** Storage for the previous temperature values */
    PreferencesStorage storage;
    
    /** Thread for update */
    Thread updateThread;
    
    /** List of callbacks */
    final RemoteCallbackList<UpdateServiceCallback> callbacks =
            new RemoteCallbackList<UpdateServiceCallback>();
    
    @Override
    public void onCreate() {
        super.onCreate();
        storage = new PreferencesStorage(
                getSharedPreferences(PREFERENCES, MODE_PRIVATE));
        updateThread = new TemperatureGetter(handler, storage.get());
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        callbacks.kill();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    /**
     *  Sends updates temperature values to the callbacks.
     */
    void sendUpdates(Bundle values) {
        final int n = callbacks.beginBroadcast();
        for (int i = 0; i < n; i++) {
            try {
                callbacks.getBroadcastItem(i).onTemperatureUpdate(values);
            } catch (RemoteException e) {
                //nothing to do
                Log.w(TAG, "callback failed", e);
            }
        }
    }
    
    /** This service implementation */
    private final UpdateServiceInterface.Stub binder = 
            new UpdateServiceInterface.Stub() {

        public void registerCallback(UpdateServiceCallback callback)
                throws RemoteException {
            if (callback == null) {
                return;
            }
            callbacks.register(callback);
        }

        public void unregisterCallback(UpdateServiceCallback callback)
                throws RemoteException {
            if (callback == null) {
                return;
            }
            callbacks.unregister(callback);
        }

        public void startUpdate() throws RemoteException {
            if (updateThread.isAlive()) {
                return;
            }
            updateThread.start();
        }

    };
    
    /**
     *  Handles temperature updates.
     */
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what != TEMPERATURE_UPDATE) {
                return;
            }
            Bundle values = msg.getData();
            sendUpdates(values);
            storage.put(values);
        }
    };

}
