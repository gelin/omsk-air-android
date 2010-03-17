package ru.opsb.myxa.android;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;

/**
 *  Broadcast receiver which receives event about boot complete.
 *  Starts UpdateService.
 */
public class BootCompletedReceiver extends BroadcastReceiver
        implements Constants {

    public void onReceive (Context context, Intent intent) {
        context.startService(UpdateService.UPDATE_ALL_INTENT);
    }

}
