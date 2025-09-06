package com.etgames.alarmme;

import android.graphics.Color;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarms")
public class Alarm {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public int hour;
    public int minute;
    public String photoUri;
    public int backGroundColor = Color.YELLOW;
    public boolean isEnabled;
    public String Description;
    public String Title;
    public boolean isRepeating;
    public boolean isDeepSleepMode;
}