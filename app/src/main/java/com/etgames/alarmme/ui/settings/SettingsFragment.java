package com.etgames.alarmme.ui.settings;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Switch;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.etgames.alarmme.AdManager;
import com.etgames.alarmme.FabViewModel;
import com.etgames.alarmme.R;
import com.etgames.alarmme.databinding.FragmentSettingsBinding;
import com.google.android.material.slider.Slider;

public class SettingsFragment extends Fragment {

    public static SharedPreferences prefs;
    FragmentSettingsBinding binding;

    FabViewModel fabViewModel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);


        fabViewModel = new ViewModelProvider(requireActivity()).get(FabViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Example: make FAB visible
        fabViewModel.setShowFab(false);
        fabViewModel.setShowQuiteFab(true);


        prefs = requireContext().getSharedPreferences("ALARM_APP", MODE_PRIVATE);
        ActivityResultLauncher<Intent> ringtonePickerLauncher;
        ringtonePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri ringtoneUri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                        if (ringtoneUri != null) {
                            Log.d("AlarmTone", "Selected ringtone: " + ringtoneUri.toString());
                            prefs.edit().putString("AlarmTone", ringtoneUri.toString()).apply();
                            // Save or use the ringtoneUri here
                        } else {
                            Log.d("AlarmTone", "User picked 'None' or canceled");
                        }
                    }
                }
        );


        Button SetRingTone = binding.SetRingTone;
        SetRingTone.setOnClickListener(v ->
        {
            Uri currentTone = Uri.parse(prefs.getString("AlarmTone", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()));
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Tone");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone);
            ringtonePickerLauncher.launch(intent);
        });
        Button supportMe = binding.supportMe;
        supportMe.setOnClickListener(v ->
        {
           AdManager.showRewarded(requireActivity());
        });

        Switch enableAds = binding.enableAds;
        enableAds.setOnCheckedChangeListener(null);
        boolean x = prefs.getBoolean("adsOn", true);
        Log.d("infoo", "the switch is " + x);
        enableAds.setChecked(x);
        enableAds.setOnCheckedChangeListener((v,state)->{


            if (state)
            {

                prefs.edit().putBoolean("adsOn",true).apply();
                boolean t = prefs.getBoolean("adsOn", true);
                Log.d("infoo", "the switch after change4 is " + t);
            }
            else
            {
                prefs.edit().putBoolean("adsOn",false).apply();
                boolean t = prefs.getBoolean("adsOn", true);
                Log.d("infoo", "the switch after change is " + t);

            }
        });

        Slider slider = view.findViewById(R.id.maxVolumeSlider);
        slider.setValue(prefs.getInt("max_volume",100));
        slider.addOnChangeListener((s, value, fromUser) -> {
            int maxVolume = (int) value;
            Log.d("infoo", "the max volume set to: "+ maxVolume);
            prefs.edit().putInt("max_volume",maxVolume).apply();
        });

        if (prefs.getBoolean("adsOn", false)) {

            FrameLayout bannerContainer = binding.bannerContainer;
            AdManager.showBanner(requireActivity(), bannerContainer);
        }

    }

}
