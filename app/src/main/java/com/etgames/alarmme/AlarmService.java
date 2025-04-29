package com.etgames.alarmme;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class AlarmService extends Service
{

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Play alarm sound or vibration
        // ...
Log.d("infoo","the service for the alarm activity started");
        Intent alarmActivityIntent = new Intent(this, AlarmActivity.class);
        alarmActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        alarmActivityIntent.putExtra("deepSleepMode",intent.getBooleanExtra("deepSleepMode",false));
        Log.d("infoo","the deepsleepmode in alarm service is " + intent.getBooleanExtra("deepSleepMode",false));

        startActivity(alarmActivityIntent);
        Log.d("infoo","the service for the alarm activity started");

        // Stop the service
       // stopSelf();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
