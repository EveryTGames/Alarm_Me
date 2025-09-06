package com.etgames.alarmme.ui.alarm;

import android.net.Uri;

import com.etgames.alarmme.Alarm;

public class AlarmUiState {

    public final Alarm alarm;
    public final boolean deepSleepMode;
    public final String displayText;
    public final String alarmDescritpionText;
    public final boolean isPasswordFieldEnabled;
    public final int backgroundColor;
    public final String photoUri;

    public AlarmUiState(
            Alarm alarm,
            boolean deepSleepMode,
            String displayText,
            boolean isPasswordFieldEnabled,
            String alarmDescritpionText,
            int backgroundColor,
            String photoUri
    ) {
        this.alarm = alarm;
        this.deepSleepMode = deepSleepMode;
        this.displayText = displayText;
        this.isPasswordFieldEnabled = isPasswordFieldEnabled;
        this.alarmDescritpionText = alarmDescritpionText;
        this.backgroundColor = backgroundColor;
        this.photoUri = photoUri;

    }
}
