package com.etgames.alarmme.ui.alarms;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.etgames.alarmme.Alarm;
import com.etgames.alarmme.AlarmsAdapter;
import com.etgames.alarmme.FabViewModel;

import com.etgames.alarmme.R;
import com.etgames.alarmme.databinding.FragmentAlarmsBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class AlarmsFragment extends Fragment {

    FragmentAlarmsBinding binding;
    FabViewModel fabViewModel;
    AlarmsViewModel alarmsViewModel;
    private ActivityResultLauncher<Intent> photoPickerLauncher;


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

    private String copyImageToInternalStorage(Uri sourceUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(sourceUri);

            // Create a unique file name
            String fileName = "alarm_image_" + System.currentTimeMillis() + ".jpg";
            File targetFile = new File(requireContext().getFilesDir(), fileName);

            OutputStream outputStream = new FileOutputStream(targetFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            return targetFile.getAbsolutePath(); // This is the path you’ll save in Room
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {

                            String internalPath = copyImageToInternalStorage(selectedImageUri);
                            if (internalPath != null) {
                                alarmsViewModel.pickPhotoResult(true);
                                alarmsViewModel.tempPhotoUri = internalPath;
                                Toast.makeText(requireContext(), "image saved successfully", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(requireContext(), "Failed to load an image, the previous photo is selected (if any)", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to load an image, the previous photo is selected (if any)", Toast.LENGTH_LONG).show();
                    }
                }
        );


        // Example: make FAB visible
        fabViewModel.setShowFab(true);
        fabViewModel.setShowQuiteFab(true);

        alarmsViewModel.photoPickListener = new AlarmsViewModel.PhotoPickListener() {
            @Override
            public void onPickPhotoRequested() {
                // This is where the Fragment actually launches the picker
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                photoPickerLauncher.launch(intent);
            }
        };
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
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = spacingInPixels;

                // Optional: Add bottom margin only for the last item
                if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
                    outRect.bottom = spacingInPixels;
                }
            }
        });
        recyclerView.setAdapter(alarmAdapter);


    }

}
