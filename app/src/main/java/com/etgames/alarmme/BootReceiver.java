package com.etgames.alarmme;

import static android.content.Context.MODE_PRIVATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("infoo", "it has been botted");

        try{

            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                // Load all enabled alarms from DB
                SharedPreferences prefs = context.getSharedPreferences("ALARM_APP", MODE_PRIVATE);
                AlarmDataBase db = AlarmDataBase.getDatabase(context);
                new Thread(() -> {
                    StringBuilder enteereddata = new StringBuilder();
                        List<Alarm> alarms = db.alarmDao().getAllEnabledAlarms();
                for (Alarm alarm : alarms) {
                    AlarmScheduler.scheduleAlarm(context, alarm);
                    enteereddata.append(alarm.id).append(" ");
                }
                    prefs.edit().putString("succesfullyRegisteredAlarmsAfterBoot",enteereddata.toString()).apply();

            }).start();
            }
        }
        catch (Exception e)
        {
            Log.w("infoo", e);
        }
    }
}