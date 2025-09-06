package com.etgames.alarmme;


import android.content.Context;

public class AlarmRepository {
    private final AlarmDao alarmDao;

    public AlarmRepository(Context context) {
        AlarmDataBase db = AlarmDataBase.getDatabase(context.getApplicationContext());
        this.alarmDao = db.alarmDao();
    }

    public Alarm getAlarmByIdSync(long id) {
        return alarmDao.getAlarmByIdSync(id);
    }
}

