package com.etgames.alarmme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Play the alarm sound
        Log.d("infoo","it got triggered");
        Intent serviceIntent = new Intent(context, AlarmService.class);
        context.startService(serviceIntent);

//        MediaPlayer mediaPlayer = MediaPlayer.create(context, Notification.DEFAULT_SOUND);
//        mediaPlayer.start();
       // System.exit(0);
        // Show a notification to alert the user
        // ...
    }
}
