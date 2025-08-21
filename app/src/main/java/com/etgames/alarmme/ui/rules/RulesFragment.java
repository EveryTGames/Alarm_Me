package com.etgames.alarmme.ui.rules;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.etgames.alarmme.AdManager;
import com.etgames.alarmme.FabViewModel;
import com.etgames.alarmme.MainActivity;
import com.etgames.alarmme.R;
import com.etgames.alarmme.RuleAdapter;
import com.etgames.alarmme.RuleWithApps;
import com.etgames.alarmme.SelectedRule;
import com.etgames.alarmme.databinding.FragmentRulesBinding;

import java.util.HashSet;
import java.util.List;

public class RulesFragment extends Fragment {

    private FragmentRulesBinding binding;
    private FabViewModel fabViewModel;
    private RulesViewModel rulesViewModel;
    private RuleAdapter ruleAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentRulesBinding.inflate(inflater, container, false);

        fabViewModel = new ViewModelProvider(requireActivity()).get(FabViewModel.class);
        rulesViewModel = new ViewModelProvider(requireActivity()).get(RulesViewModel.class);

        // Example: make FAB visible
        fabViewModel.setShowFab(true);
        fabViewModel.setShowQuiteFab(true);


        final TextView textView = binding.textAlarms;
        rulesViewModel.getText().observe(getViewLifecycleOwner(), text -> {
            if (text == null || text.isEmpty()) {
                textView.setVisibility(View.GONE);
            } else {
                textView.setText(text);
                textView.setVisibility(View.VISIBLE);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize RuleAdapter
        ruleAdapter = new RuleAdapter(ruleId -> {
            if (MainActivity.prefs.getBoolean("adsOn", false)) {
                AdManager.showInterstitial(requireActivity());
            }
            new Thread(() -> {
              //  Log.d("RULE_CLICK", "Passed ruleId: " + ruleId);
                MainActivity.db.selectedRuleDao().setSelectedRule(new SelectedRule(1, ruleId));
            }).start();
            rulesViewModel.showInputDialog(requireContext(), ruleId, false);
           // Log.d("RULE_CLICK", "Clicked rule: " + ruleId);
        });

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        //for adding margiun between list items
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
        recyclerView.setAdapter(ruleAdapter);

        // Observe all rules with their toggled apps from Room
        rulesViewModel.getAllRulesWithApps().observe(getViewLifecycleOwner(), rules -> {
            if (rules != null) {
                ruleAdapter.setRules(rules);

                // Collect all toggled rule IDs for UI toggle states
                HashSet<Long> toggledIds = new HashSet<>();
                for (RuleWithApps rwa : rules) {
                    if (rwa.rule.isEnabled) toggledIds.add(rwa.rule.id);
                }
                ruleAdapter.setToggledRuleIds(toggledIds);
            }
        });

        // FAB click to add new rule
        fabViewModel.fabClicked.observe(getViewLifecycleOwner(), unused -> {
            if (MainActivity.prefs.getBoolean("adsOn", false)) {
                AdManager.showInterstitial(requireActivity());
            }
            new Thread(() -> {
                //we are sure that it is a new rule bc clicked on the FAB
              long  lastRuleId = MainActivity.db.ruleDao().getLastRuleId() + 1;
                MainActivity.db.selectedRuleDao().setSelectedRule(new SelectedRule(1, lastRuleId));
            }).start();
            rulesViewModel.showInputDialog(requireContext(), null, true);
        });

        // Navigate to toggled apps fragment
        rulesViewModel.openAppList.observe(getViewLifecycleOwner(), unused -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_rules_to_nav_toggledapps);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d("DEBUG", "RulesFragment destroyed");
    }
}
