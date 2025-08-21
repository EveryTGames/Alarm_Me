package com.etgames.alarmme;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AlarmDao {
    @Insert
     long insert(Alarm alarm);

    @Delete
    void delete(Alarm alarm);

    @Update
     int update(Alarm alarm);

    @Query("SELECT * FROM alarms WHERE isEnabled = 1 ORDER BY hour ASC, minute ASC")
    List<Alarm> getAllEnabledAlarms();

    @Query("SELECT id FROM alarms WHERE isEnabled = 1 ORDER BY hour ASC, minute ASC")
    List<Long> getAllEnabledAlarmIds();

    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    LiveData<List<Alarm>> getAllAlarms();

    @Query("SELECT * FROM alarms WHERE id = :alarmId LIMIT 1")
    Alarm getAlarmByIdSync(long alarmId); //used in background threads only


}