package com.etgames.alarmme;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class AppNotificationListenerService extends android.service.notification.NotificationListenerService {


    private static final String LOGGER_TAG = "infoo";

    private static final int SERVICE_NOTIF_ID = 55;
    private static final String SERVICE_CHANNEL_ID = "ServiceChannel";
    private static final String CHANNEL_ID = "ALARM_CHANNEL";
    private final Handler handler = new Handler(Looper.getMainLooper());
    RuleDataBase db;
    private NotificationManager notificationManager;


    public static void restartNotificationListener(Context context) {
        ComponentName componentName = new ComponentName(context, AppNotificationListenerService.class);

        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public static void requestNotificationListenerAccess(Context context) {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        context.startActivity(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = getSharedPreferences("ALARM_APP", MODE_PRIVATE);

        boolean isOn = prefs.getBoolean("isOn", false);
        if (!isOn) {
            return;
        }
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(SERVICE_CHANNEL_ID, "Service Notifications", NotificationManager.IMPORTANCE_MIN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel.setDescription("Channel for foreground service");
        }
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(channel);
        }

        // Create and show the initial notification
        showPersistentNotification();

        // Start monitoring to auto-recover notification if swiped
        startNotificationMonitor();

        db = RuleDataBase.getDatabase(getApplicationContext());
    }

    public void AlarmNotifier(Context context) {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }


    private void startNotificationMonitor() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                boolean isNotificationShown = false;

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    StatusBarNotification[] activeNotifs = manager.getActiveNotifications();
                    for (StatusBarNotification notif : activeNotifs) {
                        if (notif.getId() == SERVICE_NOTIF_ID) {
                            isNotificationShown = true;
                            break;
                        }
                    }
                }

                if (!isNotificationShown) {
                    Log.w("infoo", "Notification was swiped or removed. Re-displaying.");
                    showPersistentNotification(); //  Recreate the notification
                }

                // Re-check every 5 seconds
                handler.postDelayed(this, 5000);
            }
        });
    }

    private void showPersistentNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Alarm Service Active")
                .setContentText("Listening for alarm commands...")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .setContentIntent(pendingIntent)  //  this opens the app when tapped
                .build();

        startForeground(SERVICE_NOTIF_ID, notification);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, "Receiving Command Notification", NotificationManager.IMPORTANCE_HIGH);

            notificationManager.createNotificationChannel(channel);
        } else {
            Toast.makeText(getApplicationContext(), "the api is below 26", Toast.LENGTH_SHORT).show();
        }
    }


    void triggerTheAlarm(boolean deepSleepMode) {

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        final Calendar myCalender = Calendar.getInstance();
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("deepSleepMode", deepSleepMode);
        Log.d("infoo", "deep sleep mode in appnotification listenerSercice is " + deepSleepMode);
        intent.setAction("com.etgames.trigerAlarm");
        // Use FLAG_UPDATE_CURRENT to ensure the new extras are respected
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_background).setContentTitle("WAKE UP").setContentText("the command received.").setDefaults(Notification.DEFAULT_SOUND).setAutoCancel(true);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(652, builder.build());


        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, myCalender.getTimeInMillis(), pendingIntent);


        //  startRepeatingNotifications(this);

        Log.d(LOGGER_TAG, "command revcived, alarm triggered");


    }

    private boolean isSenderMatch(String senderName, String targetName) {
        if (targetName == null || targetName.isEmpty()) {
            return true;
        }
        return senderName.equals(targetName);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Bundle extras = sbn.getNotification().extras;

        if (extras == null) return;

        String message = extras.getString("android.text");
        String senderName = extras.getString("android.title");
        String packageName = sbn.getPackageName();

        new Thread(() -> {
            RuleDao ruleDao = db.ruleDao();

            Set<String> toggledApps = new HashSet<>(ruleDao.getAllToggledApps());
            if (!toggledApps.contains(packageName)) return;

            List<Long> ids = ruleDao.getRuleIdsByAppName(packageName);
            if (ids == null) {
                Log.e("infoo", "No rule IDs found for package: " + packageName);
                return;
            }

            Set<Long> toggledRulesSet = new HashSet<>(ruleDao.getAllEnabledRules());

            for (Long id : ids) {
                if (!toggledRulesSet.contains(id)) continue;

                Rule ruleData = ruleDao.getRuleById(id);
                if (ruleData == null) continue;

                boolean isSender = false;
                boolean isCommand = false;

                if (ruleData.preferedSenderName == null || ruleData.preferedSenderName.isEmpty()) {
                    isSender = true;
                } else {

                    for (String _senderName : ruleData.preferedSenderName) {
                        if (isSenderMatch(senderName, _senderName)) {
                            isSender = true;
                            break;
                        }
                    }
                }

                if (ruleData.preferedContentCommands == null || ruleData.preferedContentCommands.isEmpty()) {
                    isCommand = true;
                } else {
                    for (String command : ruleData.preferedContentCommands) {
                        if (message != null && message.contains(command)) {
                            isCommand = true;
                            break;
                        }
                    }
                }

                boolean shouldTrigger = ruleData.isAnd ? (isSender && isCommand) : (isSender || isCommand);

                if (shouldTrigger) {
                    triggerAlarmOnMain(ruleData.deepSleepMode);
                    break;
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("infoo", "on start command called, service started?");
        Log.d("infoo", "the deepsleepmode in on start command for app notification listener is " + intent.getBooleanExtra("deepSleepMode", false));

        return START_STICKY;
    }


    private void triggerAlarmOnMain(boolean deepSleepMode) {
        new Handler(getMainLooper()).post(() -> triggerTheAlarm(deepSleepMode));
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("infoo", "unbind have been called, service stopped?");

        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {


        Log.d("infoo", "onbind called, service started?");

        //this part is for the repeat notification for the xiami smart watch
        AlarmNotifier(this);


        return super.onBind(intent);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null); // Prevent memory leaks
        Log.d("infoo", "destroy have been called, service stopped?");
        super.onDestroy();

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("infoo", "taskRemoved have been called, service stopped?");
        restartNotificationListener(getApplicationContext());
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("infoo", "rebind have been called, service stopped?");

        super.onRebind(intent);
    }

}