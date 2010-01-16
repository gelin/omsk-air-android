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
        callbacks.finishBroadcast();
    }
    
    /**
     *  Sends error message to callbacks.
     */
    void sendError(String error) {
        final int n = callbacks.beginBroadcast();
        for (int i = 0; i < n; i++) {
            try {
                callbacks.getBroadcastItem(i).onError(error);
            } catch (RemoteException e) {
                //nothing to do
                Log.w(TAG, "callback failed", e);
            }
        }
        callbacks.finishBroadcast();
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
            synchronized (updateThread) {       
                switch (updateThread.getState()) {  //isAlive() is bad???
                case NEW:
                case TERMINATED:
                    updateThread.start();
                }
            }
        }

    };
    
    /**
     *  Handles temperature updates.
     */
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case TEMPERATURE_UPDATE:
                Bundle values = msg.getData();
                storage.put(values);
                sendUpdates(values);
                break;
            case ERROR:
                sendError(String.valueOf(msg.obj));
                break;
            }
        }
    };

}
