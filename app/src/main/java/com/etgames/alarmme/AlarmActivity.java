package com.etgames.alarmme;


import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;

import com.etgames.alarmme.ui.alarm.AlarmViewModel;
import com.etgames.alarmme.utils.NotificationHelper;

import java.util.Random;

public class AlarmActivity extends AppCompatActivity {

    public static boolean isActive = false;
    private AlarmViewModel viewModel;

    private EditText passwordField;
    private TextView alarmDescription, passwordText, passwordInstructions;
    private ConstraintLayout background;
    private ImageView image;
    private String requiredText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        unlockScreen();
        NotificationHelper.createNotificationChannel(this);
        NotificationHelper.startRepeatingNotifications(this);

        initUI();
        initViewModel();
        handleIntent();
        setupStopButton();
    }

    private void unlockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            km.requestDismissKeyguard(this, null);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            Toast.makeText(this, "API < 27", Toast.LENGTH_SHORT).show();
        }
    }

    private void initUI() {
        passwordField = findViewById(R.id.passwordField);
        passwordText = findViewById(R.id.textView);
        passwordInstructions = findViewById(R.id.instructionText);
        alarmDescription = findViewById(R.id.alarmDescription);
        background = findViewById(R.id.mainBackGround);
        image = findViewById(R.id.Image);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(AlarmViewModel.class);
        viewModel.getUiState().observe(this, ui -> {
            if (ui == null) return;

            if (ui.photoUri != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(ui.photoUri);
                image.setImageBitmap(bitmap);
            } else {
                image.setImageResource(0);
                //TODO u will add some text that can be changed for each alarm that be displayed with the alarm, diffrent tha the password instructions and hint
                //TODO add all of these things to the create alarm dialogue so that the user can create or edit them
                Log.w("infoo", "No image URI to load");
            }
            background.setBackgroundColor(ui.backgroundColor);
            passwordText.setText(ui.displayText);
            passwordField.setEnabled(ui.isPasswordFieldEnabled);
            alarmDescription.setText(ui.alarmDescritpionText);
            requiredText = ui.displayText;

            if (!ui.deepSleepMode) {
                passwordInstructions.setText("just press the button to stop");
                passwordField.setHint("");
            }
        });
    }

    private void handleIntent() {
        boolean deepSleepMode = getIntent().getBooleanExtra("deepSleepMode", false);
        long alarmId = getIntent().getLongExtra("alarm_id", -1);

        if (alarmId != -1) {
            Log.d("infoo", " the alarm id working now  is " + alarmId);

            viewModel.loadAlarm(alarmId);
        } else {
            Log.w("infoo", "Missing alarm ID!");
        }

        viewModel.setDeepSleepMode(deepSleepMode);
    }

    private void setupStopButton() {
        findViewById(R.id.stopBtn).setOnClickListener(v -> {
            String entered = passwordField.getText().toString().trim();

            if (viewModel.getUiState().getValue() != null &&
                    viewModel.getUiState().getValue().deepSleepMode &&
                    !entered.equals(requiredText)) {

                passwordField.setError("enter that text exactly, all of it");
                return;
            }

            //  Stop the alarm-related service ONLY
            stopService(new Intent(this, AlarmService.class));

            //  Remove notification for alarm if needed
            NotificationHelper.stopNotification(this);

            //  Just finish this Activity only
            finish(); // Only closes this screen, leaves app & services alive
        });
    }

    @Override
    protected void onResume() {
        isActive = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        isActive = false;
        super.onPause();
    }
}
