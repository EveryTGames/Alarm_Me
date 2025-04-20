package com.etgames.alarmme;

import static com.etgames.alarmme.MainActivity.CHANNEL_ID;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class PersistentService extends Service {

    private static final int REQUEST_CODE = 123;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create the notification channel
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, "My Channel", NotificationManager.IMPORTANCE_DEFAULT);

            channel.setDescription("Channel  description");

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        else
        {
            Toast.makeText(getApplicationContext(), "the api is below 26", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("My  App is Running")
                .setContentText("This is a persistent notification")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);

        // Show the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(55, builder.build());


        return START_STICKY;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("infoo", "unbind have been called from persistence, service stopped?");

        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("infoo", "onbind called from persistence, service started?");
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("infoo", "destroy have been called from persistence, service stopped?");
    }
    // ... other service methods
}




