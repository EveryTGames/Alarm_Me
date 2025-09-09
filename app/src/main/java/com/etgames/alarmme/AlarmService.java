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

import java.util.LinkedList;
import java.util.Queue;

public class AlarmService extends Service {

    public static SharedPreferences prefs;
    private static boolean currentDeepSleepMode = false;
    private static long currentAlarmId = 0;
    final int[] lastPosition = {-1}; // used to track MediaPlayer stuck position
    private final Queue<Intent> alarmQueue = new LinkedList<>();
    private final Object alarmLock = new Object(); // Lock to prevent race conditions
    boolean stopped = false;
    int maxVolume;
    private boolean isAlarmRunning = false;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable launchStopScreenRunnable;

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
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP_CURRENT_ALARM".equals(intent.getAction())) {
            Log.w("infoo", "Received STOP_CURRENT_ALARM command");
            stopAlarm(); // Stop current alarm and advance the queue
            return START_STICKY;
        }

        long alarmId = intent.getLongExtra("alarm_id", 0);
        Log.d("infoo", "AlarmService received alarm ID: " + alarmId);

        synchronized (alarmLock) {
            if (isAlarmRunning) {
                Log.w("infoo", "🎈 Alarm already running — queueing new alarm: " + alarmId);
                alarmQueue.add(intent);
                return START_STICKY;
            }
            isAlarmRunning = true;
        }

        startAlarmFromIntent(intent);
        return START_STICKY;
    }

    private void startAlarmFromIntent(Intent intent) {
        currentDeepSleepMode = intent.getBooleanExtra("deepSleepMode", false);
        currentAlarmId = intent.getLongExtra("alarm_id", 0);
        Log.w("infoo", "🔔 Starting alarm ID: " + currentAlarmId + " (deep sleep: " + currentDeepSleepMode + ")");

        startForeground(1, createNotification());
        prefs = getSharedPreferences("ALARM_APP", MODE_PRIVATE);
        maxVolume = prefs.getInt("max_volume", 100);
        checkVolume();
        startAlarm();
        StartAlarmActivity(intent);

        // Handler to keep the activity alive and detect stuck MediaPlayer
        handler = new Handler(Looper.getMainLooper());
        launchStopScreenRunnable = new Runnable() {
            @Override
            public void run() {
                checkVolume();
                if (!AlarmActivity.isActive) {
                    StartAlarmActivity(intent);
                }

                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    //Log.d("infoo", "Current: " + currentPosition + ", Last: " + lastPosition[0]);

                    if (currentPosition == lastPosition[0]) {
                        Log.d("infoo", "MediaPlayer is stuck or playback finished unexpectedly, restarting it now");
                        startAlarm();
                    } else {
                        lastPosition[0] = currentPosition;
                    }
                } else {
                    Log.d("infoo", "MediaPlayer is not playing");
                }

                handler.postDelayed(this, 1500);
            }
        };
        handler.post(launchStopScreenRunnable);
    }

    /**
     * Stops the current alarm, removes the handler, and progresses the queue.
     */
    public void stopAlarm() {
        stopped = true;

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

        synchronized (alarmLock) {
            isAlarmRunning = false;

            if (!alarmQueue.isEmpty()) {
                Intent nextIntent = alarmQueue.poll();
                Log.w("infoo", "Starting next queued alarm");

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    synchronized (alarmLock) {
                        isAlarmRunning = true;
                    }
                    startAlarmFromIntent(nextIntent);
                }, 500);
            } else {

                Log.w("infoo", "No more queued alarms — stopping service");
                stopSelf(); // stop the service if nothing is left
            }
        }
    }

    void checkVolume() {
        if (BuildConfig.DEBUG) {
            // Log.d("infoo", "DEBUG mode — skipping volume check");
           // return;
        }
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int currentAlarm = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        int maxAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        int percentageMax = (int) ( maxAlarm * ( maxVolume /100.0f));
        Log.d("infoo","the aarm volume is " + percentageMax + " out of "+ maxAlarm);
        if (currentAlarm != percentageMax)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, percentageMax, 0);

        int currentNotif = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        int maxNotif = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        if (currentNotif < maxNotif)
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, maxNotif, 0);
    }

    void startAlarm() {
        stopAlarmSound();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        try {
            Uri currentTone = Uri.parse(
                    prefs.getString(
                            "AlarmTone",
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()
                    )
            );

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            mediaPlayer.setAudioAttributes(audioAttributes);
            mediaPlayer.setDataSource(this, currentTone);
            //  mediaPlayer.setOnCompletionListener((z) -> Log.d("infoo", "Alarm finished playing"));
            mediaPlayer.setScreenOnWhilePlaying(true);
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
            } catch (Exception ignored) {
            }
            mediaPlayer = null;
        }
    }

    void StartAlarmActivity(Intent intent) {
        Intent alarmActivityIntent = new Intent(this, AlarmActivity.class);
        alarmActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        alarmActivityIntent.putExtra("deepSleepMode", intent.getBooleanExtra("deepSleepMode", false));
        alarmActivityIntent.putExtra("alarm_id", intent.getLongExtra("alarm_id", 0));
        Log.d("infoo", "DeepSleepMode: " + intent.getBooleanExtra("deepSleepMode", false));
        startActivity(alarmActivityIntent);
        Log.d("infoo", "Started AlarmActivity");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopAlarm(); // Stop current alarm
        currentDeepSleepMode = false;
        currentAlarmId = 0;
        super.onDestroy();
    }
}
