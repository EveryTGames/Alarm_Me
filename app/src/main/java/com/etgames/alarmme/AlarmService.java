package com.etgames.alarmme;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
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

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("🚨 Alarm Ringing")
                .setContentText("Tap to stop the alarm in the app")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent) // this makes it clickable
                .setOngoing(true)
                .build();
    }

    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable launchStopScreenRunnable;
    public static SharedPreferences prefs;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, createNotification());

        prefs = getSharedPreferences("ALARM_APP", MODE_PRIVATE);
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
        stopAlarmSound(); // Ensure any existing playback is stopped

        mediaPlayer = new MediaPlayer();

        try {
            Uri currentTone = Uri.parse(
                    prefs.getString(
                            "AlarmTone",
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()
                    )
            );

            // Modern replacement for setAudioStreamType()
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM) // Mark as alarm
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC) // PCM/wav/mp3 content
                    .build();

            mediaPlayer.setAudioAttributes(audioAttributes);
            mediaPlayer.setDataSource(this, currentTone);

            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (Exception e) {
            Log.e("infoo", "Error starting alarm", e);
        }
    }

    void stopAlarmSound() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
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
