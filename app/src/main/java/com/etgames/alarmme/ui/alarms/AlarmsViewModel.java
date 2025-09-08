package com.etgames.alarmme.ui.alarms;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
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
import com.mrudultora.colorpicker.ColorPickerPopUp;

import java.io.File;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlarmsViewModel extends AndroidViewModel {

    private final AlarmDao alarmDao;
    private final MediatorLiveData<Pair<List<Alarm>, HashSet<String>>> alarmsLiveData = new MediatorLiveData<>();
    private final MutableLiveData<String> mText = new MutableLiveData<>();
    public PhotoPickListener photoPickListener; // Fragment sets this
    public SingleLiveEvent<Alarm> startScheduleAlarm = new SingleLiveEvent<>();
    public String tempPhotoUri;
    boolean isCurrentNewAlarm;
    Alarm currentAlarm;
    /**
     * the first is "should the image be deleted?", and the second is the path to the image
     */
    Pair<Boolean, String> flagToDeleteTheImage = new Pair<>(false, null);


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

    public void pickPhotoResult(boolean result) {

        if (result) {
            if (!isCurrentNewAlarm) {

                flagToDeleteTheImage = new Pair<>(true, currentAlarm.photoUri);
                currentAlarm.photoUri = null;
            }
        }
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<Pair<List<Alarm>, HashSet<String>>> getAlarmsLiveData() {
        return alarmsLiveData;
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

    public void showInputDialog(Context context, boolean _isCurrentNewAlarm, Alarm _currentAlarm) {
        isCurrentNewAlarm = _isCurrentNewAlarm;
        currentAlarm = _currentAlarm;
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

        if (isCurrentNewAlarm) {
            btnDelete.setEnabled(false);
        }
// Set values from alarm
        timePicker.setHour(currentAlarm.hour);
        timePicker.setMinute(currentAlarm.minute);
        editDescription.setText(currentAlarm.Description);
        switchRepeating.setChecked(currentAlarm.isRepeating);
        switchDeepSleep.setChecked(currentAlarm.isDeepSleepMode);


        dialogView.findViewById(R.id.btnAddPhoto).setOnClickListener(v -> {
            if (photoPickListener != null) {


                photoPickListener.onPickPhotoRequested();


            }
        });


        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> {


            deleteImageFileIfExists(tempPhotoUri);

            dialog.dismiss();

        });
        dialogView.findViewById(R.id.btnChangeBackGroundColor).setOnClickListener(v -> {

            ColorPickerPopUp colorPickerPopUp = new ColorPickerPopUp(context);    // Pass the context.
            colorPickerPopUp.setShowAlpha(false)            // By default show alpha is true.
                    .setDefaultColor(currentAlarm.backGroundColor)            // By default red color is set.
                    .setDialogTitle("Pick a Color")
                    .setOnPickColorListener(new ColorPickerPopUp.OnPickColorListener() {
                        @Override
                        public void onColorPicked(int color) {
                            // handle the use of color
                            currentAlarm.backGroundColor = color;
                        }

                        @Override
                        public void onCancel() {
                            colorPickerPopUp.dismissDialog();    // Dismiss the dialog.
                        }
                    })
                    .show();


        });

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            currentAlarm.hour = timePicker.getHour();
            currentAlarm.minute = timePicker.getMinute();
            currentAlarm.Title = convertTime(timePicker.getHour(), timePicker.getMinute());
            currentAlarm.Description = editDescription.getText().toString();
            currentAlarm.isRepeating = switchRepeating.isChecked();
            currentAlarm.isDeepSleepMode = switchDeepSleep.isChecked();
            //this if statment is for if the user didnt update the photo while there is already existing photo, it should be still remain there
            if (currentAlarm.photoUri == null || tempPhotoUri != null) {
                currentAlarm.photoUri = tempPhotoUri;
            }
            tempPhotoUri = null;

            if (flagToDeleteTheImage.first) {
                deleteImageFileIfExists(flagToDeleteTheImage.second);

            }
            if (isCurrentNewAlarm) {

                insertAlarm(currentAlarm, (_alarm) -> startScheduleAlarm.postValue(_alarm));
            } else {

                updateAlarm(currentAlarm, _alarm -> startScheduleAlarm.postValue(_alarm));
            }
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            //these two lines to make the alarm unscheduled before deleting if it was enabled
            if (currentAlarm.isEnabled) {

                currentAlarm.isEnabled = false;
                startScheduleAlarm.setValue(currentAlarm);
            }

            deleteImageFileIfExists(currentAlarm.photoUri);
            deleteAlarm(currentAlarm);
            dialog.dismiss();
        });


        dialog.show();
    }

    private void deleteImageFileIfExists(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            Log.w("infoo", "no image saved to delete ");

            return;
        }

        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            boolean deleted = imageFile.delete();
            if (!deleted) {
                Log.w("infoo", "Failed to delete image: " + imageFile.getAbsolutePath());
            } else {
                Log.d("infoo", "deleted image: " + imageFile.getAbsolutePath());

            }
        }
    }

    public void toggleAlarm(Alarm alarm, boolean enabled, Context context) {
        alarm.isEnabled = enabled;

        updateAlarm(alarm, null);

        if (enabled) {
            Log.d("infoo", "the alarm id being scheduled is " + alarm.id);
            AlarmScheduler.scheduleAlarm(context, alarm);

        } else {
            Log.d("infoo", "the alarm id being canceled is " + alarm.id);

            AlarmScheduler.cancelAlarm(context, alarm);
        }
    }


    public interface PhotoPickListener {
        void onPickPhotoRequested();
    }

    public interface AlarmInsertedCallback {
        void onAlarmInserted(Alarm alarm);
    }


}
