package com.etgames.alarmme;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class AlarmService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    private Notification createNotification() {
        String channelId = "alarm_channel23";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Alarm Service",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("🚨 Alarm Ringing")
                .setContentText("you have to stop the alarm from the app")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .build();
    }

    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable launchStopScreenRunnable;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, createNotification());

        checkVolume();
        startAlarm();
        StartAlarmActivity(intent);


        handler = new Handler(Looper.getMainLooper());
        launchStopScreenRunnable = new Runnable() {
            @Override
            public void run() {
                checkVolume();
                if (!AlarmActivity.isActive) {
                    StartAlarmActivity(intent);
                }

                // Re-run after 3 seconds if still active
                handler.postDelayed(this, 1500);
            }
        };
        handler.post(launchStopScreenRunnable);

        return START_STICKY;
    }

    void stopAlarm() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (handler != null && launchStopScreenRunnable != null) {
            handler.removeCallbacks(launchStopScreenRunnable);
        }
    }

    void checkVolume()
    {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int currentAlarm = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        int maxAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

        if (currentAlarm < maxAlarm) {
            // Not maxed out yet, so set it to max
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxAlarm, 0);
        }


        int currentNotif = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        int maxNotif = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);

        if (currentNotif < maxNotif) {
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, maxNotif, 0);
        }

    }
    void startAlarm() {
        // Initialize MediaPlayer
        mediaPlayer = new MediaPlayer();

        try {

            Uri currentTone = Uri.parse(MainActivity.prefs.getString("AlarmTone", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()));

            // Set the audio stream to ALARM
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);

            // Set the data source to the default alarm sound
            mediaPlayer.setDataSource(this, currentTone);

            mediaPlayer.setLooping(true); // Set to loop the sound
            mediaPlayer.prepare();       // Prepare the MediaPlayer
            mediaPlayer.start();         // Start playing
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void StartAlarmActivity(Intent intent) {
        Log.d("infoo", "the service for the alarm activity started");
        Intent alarmActivityIntent = new Intent(this, AlarmActivity.class);
        alarmActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        alarmActivityIntent.putExtra("deepSleepMode", intent.getBooleanExtra("deepSleepMode", false));
        Log.d("infoo", "the deepsleepmode in alarm service is " + intent.getBooleanExtra("deepSleepMode", false));

        startActivity(alarmActivityIntent);
        Log.d("infoo", "the service for the alarm activity started");


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        stopAlarm();
        super.onDestroy();
    }
}
