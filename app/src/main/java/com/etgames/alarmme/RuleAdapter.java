package com.etgames.alarmme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.Set;

public class RuleAdapter extends ListAdapter<String, RuleAdapter.RuleViewHolder> {

    public interface OnRuleClickListener {
        void onRuleClick(String ruleId);
    }

    private final OnRuleClickListener listener;

    public RuleAdapter(OnRuleClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<String> DIFF_CALLBACK = new DiffUtil.ItemCallback<String>() {
        @Override
        public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public RuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
        return new RuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RuleViewHolder holder, int position) {
        String ruleId = getItem(position);
        holder.bind(ruleId);
    }

    class RuleViewHolder extends RecyclerView.ViewHolder {
        TextView ruleTitle;
        com.google.android.material.materialswitch.MaterialSwitch ruleSwitch;

        public RuleViewHolder(@NonNull View itemView) {
            super(itemView);
            ruleTitle = itemView.findViewById(R.id.listItemDetails);
            ruleSwitch = itemView.findViewById(R.id.switch1);


        }




        public void bind(String ruleId) {
            boolean isToggled = toggleSet.contains(ruleId);
            ruleTitle.setText("Rule #" + ruleId);
            ruleSwitch.setOnCheckedChangeListener(null);
            ruleSwitch.setChecked(isToggled);
            ruleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

                if (isChecked) {
                    toggleSet.add(ruleId);
                    MainActivity.prefs.edit().putStringSet("toggledRules",toggleSet).apply();




                } else {
                    toggleSet.remove(ruleId);

                    MainActivity.prefs.edit().putStringSet("toggledRules", toggleSet).apply();
                    //u will also make the logivc for saving the texts like the prefered command and prefered name
                    // u will use shared preference normally
                    // key: "package name"    value : "<preferered sender name><"or"  or "and" to determine if the user wants the sender name and one of the content messages to match in order to trigger the alarm, or just want any one of them>:#94710341#:<prefered content message 1>:#94710341#:<prefered content message 2 ... and so on>"
                }
            });



            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onRuleClick(ruleId);
            });

        }
    }



    private Set<String> toggleSet = new HashSet<>();

    public void setToggleState(Set<String> toggleSet) {
        this.toggleSet = toggleSet != null ? toggleSet : new HashSet<>();
        notifyDataSetChanged(); // refresh all views with new toggle state
    }
}
