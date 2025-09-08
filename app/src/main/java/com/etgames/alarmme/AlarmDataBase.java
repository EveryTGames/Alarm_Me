package com.etgames.alarmme;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Alarm.class}, version = 4)
public abstract class AlarmDataBase extends RoomDatabase {

    private static volatile AlarmDataBase INSTANCE;

    public abstract AlarmDao alarmDao();

    public static AlarmDataBase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AlarmDataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder (context.getApplicationContext(),
                                    AlarmDataBase.class, "alarm_db")
                            .fallbackToDestructiveMigration(true)
                            .build();
                }
            }
            Log.d("infoo", "Created AlarmDatabase instance: " + System.identityHashCode(INSTANCE));
        } else {
            Log.d("infoo", "Reused AlarmDatabase instance: " + System.identityHashCode(INSTANCE));
        }
        return INSTANCE;
    }
}
