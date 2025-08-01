package com.etgames.alarmme.ui.alarms;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.etgames.alarmme.Alarm;
import com.etgames.alarmme.AlarmsAdapter;
import com.etgames.alarmme.FabViewModel;

import com.etgames.alarmme.databinding.FragmentAlarmsBinding;

public class AlarmsFragment extends Fragment {

    FragmentAlarmsBinding binding;
    FabViewModel fabViewModel;
    AlarmsViewModel alarmsViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAlarmsBinding.inflate(inflater, container, false);


        fabViewModel = new ViewModelProvider(requireActivity()).get(FabViewModel.class);
        alarmsViewModel = new ViewModelProvider(requireActivity()).get(AlarmsViewModel.class);


        final TextView textView = binding.textAlarms;

        alarmsViewModel.getText().observe(getViewLifecycleOwner(), text -> {
            if (text == null || text.isEmpty()) {
                textView.setVisibility(View.GONE);
            } else {
                textView.setText(text);
                textView.setVisibility(View.VISIBLE);
            }
        });

        return binding.getRoot();

    }

    static AlarmsAdapter alarmAdapter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Example: make FAB visible
        fabViewModel.setShowFab(true);
        fabViewModel.setShowQuiteFab(true);
        // Example: set FAB click behavior for this fragment
        fabViewModel.fabClicked.observe(getViewLifecycleOwner(), unused -> {
            // Handle the click once!

            try {
                alarmsViewModel.showInputDialog(requireContext(), true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

           // Toast.makeText(requireContext(), "FAB clicked! in alarms", Toast.LENGTH_SHORT).show();
        });


        alarmsViewModel.startScheduleAlarm.observe(getViewLifecycleOwner(), alarm -> alarmsViewModel.toggleAlarm(alarm, alarm.isEnabled, requireContext()));




        alarmAdapter = new AlarmsAdapter(new AlarmsAdapter.OnAlarmClickListener() {
            @Override
            public void onAlarmClick(Alarm alarm) {
                // handle click
                alarmsViewModel.showInputDialog(requireContext(), false, alarm);

                Log.d("infoo", "Clicked alarm: " + alarm.id + " is enabled? " + alarm.isEnabled);
            }

            @Override
            public void onAlarmToggle(Alarm alarm, boolean isEnabled) {
                alarm.isEnabled = isEnabled;
                alarmsViewModel.toggleAlarm(alarm, isEnabled, requireContext());

            }
        });


        alarmsViewModel.getAlarmsLiveData().observe(getViewLifecycleOwner(), alarms -> {
            alarmAdapter.setToggleState(alarms.second);
            alarmAdapter.submitList(alarms.first);
        });

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(alarmAdapter);


    }

}
