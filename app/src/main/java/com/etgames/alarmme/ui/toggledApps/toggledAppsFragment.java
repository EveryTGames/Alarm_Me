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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.etgames.alarmme.FabViewModel;
import com.etgames.alarmme.MainActivity;
import com.etgames.alarmme.R;
import com.etgames.alarmme.SelectedRule;
import com.etgames.alarmme.Rule;
import com.etgames.alarmme.appListAdapter;
import com.etgames.alarmme.app_info;
import com.etgames.alarmme.databinding.FragmentToggledappsBinding;
import com.etgames.alarmme.ui.rules.RulesViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class toggledAppsFragment extends Fragment {

    private Runnable searchRunnable;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService background = Executors.newSingleThreadExecutor();
    private final int delay = 1000;

    private appListAdapter adapter;
    private FabViewModel fabViewModel;
    private RulesViewModel rulesViewModel;
    private FragmentToggledappsBinding binding;

    private long currentRuleId = 0;

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

        fabViewModel.setShowFab(false);
        fabViewModel.setShowQuiteFab(false);

        setupRecyclerView();
        setupSearch();
        setupConfirmButton();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        background.execute(() -> {
            // Get selected rule
            currentRuleId = MainActivity.db.selectedRuleDao().getSelectedRuleId();



            // Load toggled apps safely
            List<String> toggledAppsForRule = MainActivity.db.ruleDao().getToggledAppsForRule(currentRuleId);

            mainHandler.post(() -> {
                adapter = new appListAdapter(requireContext(), currentRuleId);
                adapter.setToggleState(new HashSet<>(toggledAppsForRule));
                adapter.submitList(loadedApps);
                recyclerView.setAdapter(adapter);
            });
        });
    }

    private void setupSearch() {
        EditText searchEditText = binding.searchEditText;
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable editable) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (searchRunnable != null) mainHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> filter(charSequence.toString().trim());
                mainHandler.postDelayed(searchRunnable, delay);
            }
        });

        ImageButton deleteSearchBtn = binding.delBtn;
        deleteSearchBtn.setOnClickListener(v -> searchEditText.setText(""));
    }

    private void setupConfirmButton() {
        Button btnConfirm = binding.btnToggledAppsConfirm;
        btnConfirm.setOnClickListener(view -> {
            // Always call dialog on main thread
            mainHandler.post(() -> rulesViewModel.dialogg.show());

            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_toggledapps_to_nav_rules);
        });
    }

    private void filter(@NonNull String searchQuery) {
        background.execute(() -> {
            List<app_info> filteredList = new ArrayList<>();
            if (!searchQuery.isEmpty()) {
                for (app_info item : loadedApps) {
                    if (item.getItemTitle().toLowerCase().contains(searchQuery.toLowerCase())) {
                        filteredList.add(item);
                    }
                }
            } else {
                mainHandler.post(() -> adapter.submitList(loadedApps));
                return;
            }
            mainHandler.post(() -> adapter.submitList(filteredList));
        });
    }
}
