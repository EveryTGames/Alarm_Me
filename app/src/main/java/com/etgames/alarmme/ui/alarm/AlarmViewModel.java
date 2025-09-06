package com.etgames.alarmme.ui.alarm;

import android.app.Application;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.etgames.alarmme.Alarm;
import com.etgames.alarmme.AlarmRepository;

import java.util.Random;


public class AlarmViewModel extends AndroidViewModel {

    private final AlarmRepository repository;
    private final MutableLiveData<AlarmUiState> uiState = new MutableLiveData<>();
    private Alarm loadedAlarm;
    private boolean deepSleepMode;

    public AlarmViewModel(@NonNull Application application) {
        super(application);
        this.repository = new AlarmRepository(application);
    }

    public LiveData<AlarmUiState> getUiState() {
        return uiState;
    }

    public void setDeepSleepMode(boolean mode) {
        this.deepSleepMode = mode;
        updateUiState();
    }

    public void loadAlarm(long alarmId) {
        new Thread(() -> {
            loadedAlarm = repository.getAlarmByIdSync(alarmId);
            updateUiState();
        }).start();
    }

    private void updateUiState() {
        String displayText;
        boolean isPasswordFieldEnabled;
        if (deepSleepMode) {
            String fixedText = "okay i will wake up, nop im alright ";
            String chars = "ABCDEFGHJKLMNOPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz0123456789";
            StringBuilder randomBuilder = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                randomBuilder.append(chars.charAt(random.nextInt(chars.length())));
            }
            displayText = fixedText + randomBuilder.toString();
            isPasswordFieldEnabled = true;
        } else {
            displayText = "";
            isPasswordFieldEnabled = false;
        }
        if (loadedAlarm == null) {
            AlarmUiState newState = new AlarmUiState(
                    null,
                    deepSleepMode,
                    displayText,
                    isPasswordFieldEnabled,
                    "",
                    Color.YELLOW,
                    null
            );

            uiState.postValue(newState);
            return;
        }



        AlarmUiState newState = new AlarmUiState(
                loadedAlarm,
                deepSleepMode,
                displayText,
                isPasswordFieldEnabled,
                loadedAlarm.Description,
                loadedAlarm.backGroundColor,
                loadedAlarm.photoUri
        );

        uiState.postValue(newState);
    }
}

