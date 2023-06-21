package com.washington.chattertrace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.washington.chattertrace.DataLogic.DataManager;
import com.washington.chattertrace.RecordingLogic.RecordingManager;

public class NotificationActionReceiver extends BroadcastReceiver {

//    private RecordingManager recordingManager = null;
//    private DataManager dataManager = null;
//
//    public NotificationActionReceiver(RecordingManager rm, DataManager dm) {
//        this.recordingManager = rm;
//        this.dataManager = dm;
//    }

    private void sendBroadcast(Context context, String action) {
        Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        sendBroadcast(context.getApplicationContext(), "com.washington.chattertrace.GET_RECORDING");
        // Execute your desired function or trigger an action here
        // This will be called when the user taps on the notification

    }
}