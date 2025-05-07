package com.etgames.alarmme;


import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

public class AlarmActivity extends AppCompatActivity {
    public static boolean isActive = false; // Track if the screen is open



    private static final int NOTIFICATION_ID = 123;
    private NotificationManager notificationManager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean alarmActive = false;
    public void AlarmNotifier(Context context) {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
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
    public static final String CHANNEL_ID = "myAlarm";

    private Notification buildNotification(Context context) {
        return new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(android.R.drawable.ic_dialog_alert).setContentTitle("Alarm Ringing!").setContentText("").setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).build();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, "Alarm Notifications", NotificationManager.IMPORTANCE_HIGH);

            notificationManager.createNotificationChannel(channel);
        } else {
            Toast.makeText(getApplicationContext(), "the api is below 26", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("infoo", "the activity srtarted");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);

            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);

            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        } else {
            Toast.makeText(getApplicationContext(), "the api is below 27", Toast.LENGTH_SHORT).show();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);


       AlarmNotifier(this);

        boolean deepSleepMode = getIntent().getBooleanExtra("deepSleepMode",false);
        Log.d("infoo","the deepsleepmode in alarm activity is " + deepSleepMode);
        Button stopButton = findViewById(R.id.stopBtn);
        EditText password = findViewById(R.id.passwordField);
        TextView passwordText = findViewById(R.id.textView);
        TextView passwordInstructions = findViewById(R.id.textView9);

        if(!deepSleepMode)
        {
            passwordInstructions.setText("just press the button to stop");
            passwordText.setText("");
            password.setHint("");
            password.setEnabled(false);

        }

        startRepeatingNotifications(this);


        stopButton.setOnClickListener(v -> {

            Log.d("infoo", "the entered text : " + password.getText().toString().trim());
            Log.d("infoo", "the required text : " + passwordText.getText().toString().trim());
            if ( (!password.getText().toString().trim().equals(passwordText.getText().toString().trim())) && deepSleepMode )
            {
                password.setError("enter that text exactly, all of it");
                return;
            }
            Intent stopIntent = new Intent(this, AlarmService.class);
            stopService(stopIntent);

            // Remove the permanent notification
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(55);
            }


            // Close the app completely
            finishAffinity(); // Close all activities
            System.exit(0);   // Force the app process to stop



        });


    }

    @Override
    protected void onResume() {
        isActive = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        isActive= false;
        super.onPause();
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

}
