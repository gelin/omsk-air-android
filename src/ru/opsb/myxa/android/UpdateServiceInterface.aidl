package ru.opsb.myxa.android;

import ru.opsb.myxa.android.UpdateServiceCallback;

/**
 *  Temperature update service interface.
 *  Update process started on startUpdate() call.
 *  When update is finished, the result is pushed to registered
 *  callbacks.
 */
interface UpdateServiceInterface {

    /**
     *  Registers callback for the temperature update.
     */
    void registerCallback(UpdateServiceCallback callback);
    
    /**
     *  Unregisters callback for the temperature update.
     */
    void unregisterCallback(UpdateServiceCallback callback);
    
    /**
     *  Starts the temperature update process. If the updating
     *  is performed now, nothing is happens.
     */
    void startUpdate();

}