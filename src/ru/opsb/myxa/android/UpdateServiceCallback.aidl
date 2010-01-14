package ru.opsb.myxa.android;

/**
 *  Interface for temperature update service callbacks.
 *  The service will call the callbacks when the temperature value
 *  is updated.
 */
oneway interface UpdateServiceCallback {

    /**
     *  Called when new temperature value is got.
     */
    void onTemperatureUpdate(in Bundle values);

}