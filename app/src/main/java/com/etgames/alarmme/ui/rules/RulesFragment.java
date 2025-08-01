package com.etgames.alarmme.ui.rules;

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

import com.etgames.alarmme.FabViewModel;
import com.etgames.alarmme.MainActivity;
import com.etgames.alarmme.R;
import com.etgames.alarmme.RuleAdapter;
import com.etgames.alarmme.databinding.FragmentRulesBinding;



public class RulesFragment extends Fragment {

    FragmentRulesBinding binding;
    FabViewModel fabViewModel;
    MainActivity mainActivity;

    private RulesViewModel rulesViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRulesBinding.inflate(inflater, container, false);

        fabViewModel = new ViewModelProvider(requireActivity()).get(FabViewModel.class);
        // Initialize the fragment-scoped ViewModel
        rulesViewModel = new ViewModelProvider(requireActivity()).get(RulesViewModel.class);

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
    static RuleAdapter ruleAdapter;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity) getActivity();

        // Example: make FAB visible
        fabViewModel.setShowFab(true);
        fabViewModel.setShowQuiteFab(true);


        // Observe FAB click
        fabViewModel.fabClicked.observe(getViewLifecycleOwner(), unused -> rulesViewModel.showInputDialog(requireContext(), Integer.toString(MainActivity.lastID + 1),true));

        rulesViewModel.openAppList.observe(getViewLifecycleOwner(), unused -> {

            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_rules_to_nav_toggledapps);
        });


        rulesViewModel.getAllRules().observe(getViewLifecycleOwner(), rules -> {
            ruleAdapter.setToggleState(rules.second);
            ruleAdapter.submitList(rules.first);
        });


        ruleAdapter = new RuleAdapter(ruleId -> {
            // Handle click: load apps for this rule
            rulesViewModel.showInputDialog(requireContext(), ruleId,false);

            Log.d("RULE_CLICK", "Clicked rule: " + ruleId);
        });

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(ruleAdapter);





    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("DEBUG", "Fragment destroyed");
    }
}
