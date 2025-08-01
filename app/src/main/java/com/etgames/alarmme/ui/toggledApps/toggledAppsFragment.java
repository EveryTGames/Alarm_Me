package com.etgames.alarmme.ui.toggledApps;

import static com.etgames.alarmme.MainActivity.loadedApps;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.etgames.alarmme.FabViewModel;
import com.etgames.alarmme.MainActivity;
import com.etgames.alarmme.R;
import com.etgames.alarmme.appListAdapter;
import com.etgames.alarmme.app_info;
import com.etgames.alarmme.databinding.FragmentToggledappsBinding;
import com.etgames.alarmme.ui.rules.RulesViewModel;


public class toggledAppsFragment extends Fragment {
    Runnable searchRunnable;
    Handler handler = new Handler(Looper.getMainLooper());
    int delay = 1000;
    appListAdapter adapter;
    RulesViewModel rulesViewModel;

    FragmentToggledappsBinding binding;
    FabViewModel fabViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentToggledappsBinding.inflate(inflater, container, false);



        fabViewModel = new ViewModelProvider(requireActivity()).get(FabViewModel.class);
        rulesViewModel = new ViewModelProvider(requireActivity()).get(RulesViewModel.class);



        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Example: make FAB visible
        fabViewModel.setShowFab(false);
        fabViewModel.setShowQuiteFab(false);


        MainActivity.toggledAppsForCurrentRule=MainActivity.getToggledAppsForRule(MainActivity.currentID);

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new appListAdapter(requireContext());

        adapter.setToggleState(MainActivity.toggledAppsForCurrentRule);
        adapter.submitList(loadedApps);


        recyclerView.setAdapter(adapter);


        EditText searchEditText = binding.searchEditText;
        searchEditText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        if (searchRunnable != null) {
                            handler.removeCallbacks(searchRunnable);
                        }

                        searchRunnable = () -> {
                            String searchQuery = charSequence.toString().trim();
                            filter(searchQuery);

                        };

                        handler.postDelayed(searchRunnable, delay);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                }
        );
        ImageButton deleteSearchBtn = binding.delBtn;
        deleteSearchBtn.setOnClickListener(v ->
                searchEditText.setText("")
        );

        Button btnConfirm = binding.btnToggledAppsConfirm;
        btnConfirm.setOnClickListener(view1 -> {
            rulesViewModel.dialogg.show();
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_toggledapps_to_nav_rules);

        });
    }

    ExecutorService background = Executors.newSingleThreadExecutor();

    void filter(@NonNull String searchQuery) {
        background.execute(

                () ->
                {
                    List<app_info> filteredList;


                    if (!searchQuery.isEmpty()) {
                        filteredList = new ArrayList<>();
                        for (app_info item : loadedApps) {
                            if (item.getItemTitle().toLowerCase().contains(searchQuery.toLowerCase()))
                                filteredList.add(item);
                        }
                    } else {
                        handler.post(() -> {
                            adapter.submitList(loadedApps);
                        });
                        return;
                    }


                    handler.post(() -> {
                        adapter.submitList(filteredList);
                    });
                }
        );


    }

}
