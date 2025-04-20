package com.etgames.alarmme;


import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity {



    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);

            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
        }
        else
        {
            Toast.makeText(getApplicationContext(), "the api is below 27", Toast.LENGTH_SHORT).show();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);


        // Initialize MediaPlayer
        mediaPlayer = new MediaPlayer();

        try {
            // Set the audio stream to ALARM
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);

            // Set the data source to the default alarm sound
            mediaPlayer.setDataSource(this, Settings.System.DEFAULT_ALARM_ALERT_URI);

            mediaPlayer.setLooping(true); // Set to loop the sound
            mediaPlayer.prepare();       // Prepare the MediaPlayer
            mediaPlayer.start();         // Start playing
        } catch (Exception e) {
            e.printStackTrace();
        }


        Log.d("infoo","the activity srtarted");
        }


        public void Silence(View e)
        {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }

            // Remove the permanent notification
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(55);
            }


            // Close the app completely
            finishAffinity(); // Close all activities
            System.exit(0);   // Force the app process to stop
        }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}
