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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity {


    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("infoo", "the activity srtarted");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);

            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
        } else {
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

        stopButton.setOnClickListener(v -> {

            Log.d("infoo", "the entered text : " + password.getText().toString().trim());
            Log.d("infoo", "the required text : " + passwordText.getText().toString().trim());
            if ( (!password.getText().toString().trim().equals(passwordText.getText().toString().trim())) && deepSleepMode )
            {
                password.setError("enter that text exactly, all of it");
                return;
            }
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



        });


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
