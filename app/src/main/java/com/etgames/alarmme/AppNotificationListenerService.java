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
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;


public class AppNotificationListenerService extends android.service.notification.NotificationListenerService {


    private static final String LOGGER_TAG = "infoo";


    private static final String CHANNEL_ID = "ALARM_CHANNEL";
    private static final int NOTIFICATION_ID = 123;
    private NotificationManager notificationManager;
    private final Handler handler = new Handler();
    private boolean alarmActive = false;

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    "MyServiceChannel",
                    "Service Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel.setDescription("Channel for foreground service");
        }
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "MyServiceChannel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Notification Listener Active")
                .setContentText("Listening for alarm commands...")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true);

        startForeground(55, builder.build());
    }


    public void AlarmNotifier(Context context) {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
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

    private Notification buildNotification(Context context) {
        return new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(android.R.drawable.ic_dialog_alert).setContentTitle("Alarm Ringing!").setContentText("Tap to dismiss").setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).build();
    }

    void triggerTheAlarm(boolean deepSleepMode) {

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        final Calendar myCalender = Calendar.getInstance();
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("deepSleepMode", deepSleepMode);
        Log.d("infoo", "deep sleep mode in appnotification listenerSercice is " + deepSleepMode);
        intent.setAction("com.etgames.trigerAlarm");
        // Use FLAG_UPDATE_CURRENT to ensure the new extras are respected
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_background).setContentTitle("WAKE UP").setContentText("the command received.").setDefaults(Notification.DEFAULT_SOUND).setAutoCancel(true);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(652, builder.build());


        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, myCalender.getTimeInMillis(), pendingIntent);


        //  startRepeatingNotifications(this);

        Log.d(LOGGER_TAG, "command revcived, alarm triggered");


    }

    void startRepeatingNotifications(Context context) {
        if (alarmActive) return; // Avoid multiple triggers
        alarmActive = true;

        Runnable sendNotification = new Runnable() {
            @Override
            public void run() {
                if (!alarmActive) return;
                notificationManager.notify(NOTIFICATION_ID, buildNotification(context));
                handler.postDelayed(this, 2000); // Sends notification every 2 seconds
            }
        };

        handler.post(sendNotification);
    }

    public void stopNotifications() {
        alarmActive = false;
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private boolean isSenderMatch(String senderName, String targetName) {
        if (targetName == null || targetName.isEmpty()) {
            return true;
        }
        return senderName.equals(targetName);
    }

    public static void restartNotificationListener(Context context) {
        ComponentName componentName = new ComponentName(context, AppNotificationListenerService.class);

        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );

        pm.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );
    }


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        SharedPreferences prefs = getSharedPreferences("ALARM_APP", MODE_PRIVATE);
        boolean isOn = prefs.getBoolean("isOn", false);
        if (!isOn) {
            return;
        }
        // Handle notification events here
        Bundle extras = sbn.getNotification().extras;

        if (extras != null) {
            // Log.d("infoo","extras not null");

            String message = extras.getString("android.text");
            String SenderName = extras.getString("android.title");
            String packageName = sbn.getPackageName();
            PackageDetailss pd = new PackageDetailss(this);
           Set<String> toggledApps = PackageDetailss.prefs.getStringSet("toggled_apps", new HashSet<>());
            if (toggledApps.contains(packageName)) {
                String[] ids = PackageDetailss.getRulesIDs(packageName);
                if(ids==null)
                {
                    Log.e("infoo","wait what? in notificationlistener, something is really wrong, the package name is in the toggled list, but doesnt have any IDs");
                    return;
                }
                for (String id :
                        ids) {
                    Set<String> toggledRulesSet = PackageDetailss.prefs.getStringSet("toggledRules",new HashSet<>());

                    if(!toggledRulesSet.contains(id))
                    {
                        continue;
                    }
                    pd = pd.retrieve(id);


                    if (message != null && SenderName != null) {

                        boolean isSender = false;
                        boolean isCommand = false;


                        //here we now sure that it is one of our apps
                        for (String _senderName :
                                pd.preferedSenderName) {
                            //change  :-    remember to overwrite the equal for this, so if the _senderName is "" then it be always true
                            if (isSenderMatch(SenderName, _senderName)) {
                                isSender = true;
                                break;
                            }

                        }
                        for (String command :
                                pd.preferedContentCommands) {

                            if (message.contains(command)) {
                                isCommand = true;
                                break;
                            }

                        }

                        if (pd.isAnd) {
                            if (isSender && isCommand) {
                                triggerTheAlarm(pd.deepSleepMode);
                                break;

                            }

                        } else if (isSender || isCommand) {

                            triggerTheAlarm(pd.deepSleepMode);
                            break;
                        }

                    }
                }
            } else {
                return;
            }

        }
    }


    public static void requestNotificationListenerAccess(Context context) {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        context.startActivity(intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("infoo", "on start command called, service started?");
        Log.d("infoo", "the deepsleepmode in on start command for app notification listener is " + intent.getBooleanExtra("deepSleepMode", false));

        return super.onStartCommand(intent, flags, startId);
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