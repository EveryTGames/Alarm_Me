package com.etgames.alarmme;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class AlarmScheduler {
    public static void scheduleAlarm(Context context, Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.hour);
        calendar.set(Calendar.MINUTE, alarm.minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("deepSleepMode", alarm.isDeepSleepMode);
        intent.putExtra("alarm_id", alarm.id);
        Log.d("infoo"," the alarm id set is "  + alarm.id);

        intent.setAction("com.etgames.trigerAlarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) alarm.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );

        Log.d("infoo", "Alarm scheduled for " + alarm.Title);
    }

    public static void cancelAlarm(Context context, Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("deepSleepMode", alarm.isDeepSleepMode);
        intent.putExtra("alarm_id", alarm.id);
        Log.d("infoo"," the alarm id cancel is "  + alarm.id);
        intent.setAction("com.etgames.trigerAlarm");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) alarm.id, // ✅ must be same as the one used when setting
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            Log.d("infoo", "Alarm with ID " + alarm.id + " cancelled.");
        }
    }
}
