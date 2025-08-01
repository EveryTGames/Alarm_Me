package com.etgames.alarmme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;


public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        long alarmId = intent.getLongExtra("alarm_id", -1);
        if (alarmId == -1)
            Log.w("AlarmReceiver", "Missing alarm ID! this should be only when triggering a command alarm");
        else {


            new Thread(() -> {

                AlarmDataBase db = AlarmDataBase.getDatabase(context);
                AlarmDao alarmDao = db.alarmDao();
                Alarm alarm = alarmDao.getAlarmByIdSync(alarmId);

                if (alarm != null) {
                    if (alarm.isRepeating) {
                        AlarmScheduler.scheduleAlarm(context, alarm);
                    } else {
                        alarm.isEnabled =false;
                        alarmDao.update(alarm);

                    }

                }
            }).start();
        }

        // Play the alarm sound
        Log.d("infoo", "it got triggered");
        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra("deepSleepMode", intent.getBooleanExtra("deepSleepMode", false));
        Log.d("infoo", "the deepsleepmode in alarm receiver is " + intent.getBooleanExtra("deepSleepMode", false));

        ContextCompat.startForegroundService(context, serviceIntent);


//        MediaPlayer mediaPlayer = MediaPlayer.create(context, Notification.DEFAULT_SOUND);
//        mediaPlayer.start();
        // System.exit(0);
        // Show a notification to alert the user
        // ...
    }


}
