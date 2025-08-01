package com.etgames.alarmme.ui.alarms;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.etgames.alarmme.Alarm;
import com.etgames.alarmme.AlarmDao;
import com.etgames.alarmme.AlarmDataBase;
import com.etgames.alarmme.AlarmScheduler;
import com.etgames.alarmme.MainActivity;
import com.etgames.alarmme.R;
import com.etgames.alarmme.SingleLiveEvent;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlarmsViewModel extends AndroidViewModel {

    private final AlarmDao alarmDao;
    private final MediatorLiveData<Pair<List<Alarm>, HashSet<String>>> alarmsLiveData = new MediatorLiveData<>();
    public SingleLiveEvent<Alarm> startScheduleAlarm = new SingleLiveEvent<>();
    private final MutableLiveData<String> mText = new MutableLiveData<>();



    public AlarmsViewModel(@NonNull Application application) {
        super(application);

        alarmDao = AlarmDataBase.getDatabase(application.getApplicationContext()).alarmDao();

        LiveData<List<Alarm>> dbLiveData = alarmDao.getAllAlarms();

        mText.setValue("no alarms added, add one by clicking on the \"+\" symbol.");
        alarmsLiveData.addSource(dbLiveData, alarms -> {
            Set<String> toggledSet = MainActivity.prefs.getStringSet("toggledAlarms", new HashSet<>());
            alarmsLiveData.setValue(new Pair<>(alarms, new HashSet<>(toggledSet)));

            if (alarms == null || alarms.isEmpty()) {
                mText.setValue("no alarms added, add one by clicking on the \"+\" symbol.");
            } else {
                mText.setValue(null); // You can also use null or just leave it empty to hide it
            }
        });


    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<Pair<List<Alarm>, HashSet<String>>> getAlarmsLiveData() {
        return alarmsLiveData;
    }

    public interface AlarmInsertedCallback {
        void onAlarmInserted(Alarm alarm);
    }

    public void insertAlarm(Alarm alarm, AlarmInsertedCallback callback) {
        //all the new alarms be enabled by default
        alarm.isEnabled = true;

        new Thread(() -> {
            long result = alarmDao.insert(alarm);
            Log.d("infoo", "Inserted with ID: " + result);

            Alarm newAlarm = alarmDao.getAlarmByIdSync(result);
            if (callback != null) {
                callback.onAlarmInserted(newAlarm); // Pass back full Alarm object
            }

            Set<String> updatedToggledSet = new HashSet<>(MainActivity.prefs.getStringSet("toggledAlarms", new HashSet<>()));
            updatedToggledSet.add(Long.toString(result));
            MainActivity.prefs.edit().putStringSet("toggledAlarms", updatedToggledSet).apply();
            Log.d("infoo", "toggled alarms updated with inserted ID: " + result);
        }).start();
    }

    public void updateAlarm(Alarm alarm, AlarmInsertedCallback callback) {
        new Thread(() -> {
            int result = alarmDao.update(alarm);
            ///ToDO at any used alarm.id add a condition that will show a warning log if id == 0, it might give a bug, it shouldnt be zero mostly
            Alarm newAlarm = alarmDao.getAlarmByIdSync(alarm.id);

            if (callback != null) {
                callback.onAlarmInserted(newAlarm); // Pass back full Alarm object
            }

            Log.d("infoo", "number of updated rows " + result);
        }).start();
    }


    public void deleteAlarm(Alarm alarm) {
        Set<String> updatedToggledSet = new HashSet<>(MainActivity.prefs.getStringSet("toggledAlarms", new HashSet<>()));
        updatedToggledSet.remove(Long.toString(alarm.id));
        MainActivity.prefs.edit().putStringSet("toggledAlarms", updatedToggledSet).apply();
        new Thread(() -> alarmDao.delete(alarm)).start();
    }


    public void showInputDialog(Context context, boolean newAlarm) throws Exception {
        // Call the main method with a new Alarm
        Alarm alarm;
        if (newAlarm) {
            Calendar calendar = Calendar.getInstance();

            alarm = new Alarm();

            alarm.hour = calendar.get(Calendar.HOUR_OF_DAY);
            alarm.minute = calendar.get(Calendar.MINUTE);
            alarm.isEnabled = true;


        } else {
            throw new Exception("you called a new alarm function but set it to false??");
        }
        showInputDialog(context, newAlarm, alarm);
    }

    String convertTime(int hourOfDay, int minute) {
        int hour = hourOfDay;
        String amPm;

        // Determine AM or PM and adjust hour
        if (hour == 0) {
            hour += 12;
            amPm = "AM";
        } else if (hour == 12) {
            amPm = "PM";
        } else if (hour > 12) {
            hour -= 12;
            amPm = "PM";
        } else {
            amPm = "AM";
        }

        // Format hour and minute for display
        String formattedHour = (hour < 10) ? "0" + hour : String.valueOf(hour);
        String formattedMinute = (minute < 10) ? "0" + minute : String.valueOf(minute);


        return formattedHour + " : " + formattedMinute + " " + amPm;
    }


    public void showInputDialog(Context context, boolean newAlarm, Alarm alarm) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.alarm_details_dialouge, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); //to not disappear when the user miss-click on the outside (i hated it so much when happens to me XD )

        TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
        EditText editDescription = dialogView.findViewById(R.id.editDescription);
        Switch switchRepeating = dialogView.findViewById(R.id.switchRepeating);
        Switch switchDeepSleep = dialogView.findViewById(R.id.switchDeepSleep);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        if (newAlarm) {
            btnDelete.setEnabled(false);
        }
// Set values from alarm
        timePicker.setHour(alarm.hour);
        timePicker.setMinute(alarm.minute);
        editDescription.setText(alarm.Description);
        switchRepeating.setChecked(alarm.isRepeating);
        switchDeepSleep.setChecked(alarm.isDeepSleepMode);


        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            alarm.hour = timePicker.getHour();
            alarm.minute = timePicker.getMinute();
            alarm.Title = convertTime(timePicker.getHour(), timePicker.getMinute());
            alarm.Description = editDescription.getText().toString();
            alarm.isRepeating = switchRepeating.isChecked();
            alarm.isDeepSleepMode = switchDeepSleep.isChecked();

            if (newAlarm) {

                insertAlarm(alarm, (_alarm) -> startScheduleAlarm.postValue(_alarm));
            } else {

                updateAlarm(alarm,_alarm -> startScheduleAlarm.postValue(_alarm));
            }
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v ->
        {

            startScheduleAlarm.setValue(alarm);
            deleteAlarm(alarm);
            dialog.dismiss();
        });


        dialog.show();
    }


    public void toggleAlarm(Alarm alarm, boolean enabled, Context context) {
        alarm.isEnabled = enabled;

        updateAlarm(alarm,null);

        if (enabled) {
            Log.d("infoo", "the alarm id being scheduled is " + alarm.id);
            AlarmScheduler.scheduleAlarm(context, alarm);

        } else {
            Log.d("infoo", "the alarm id being canceled is " + alarm.id);

            AlarmScheduler.cancelAlarm(context, alarm);
        }
    }


}
