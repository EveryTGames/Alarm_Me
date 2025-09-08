package com.etgames.alarmme;

import static com.google.android.material.R.color.design_default_color_error;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.etgames.alarmme.ui.rules.RulesViewModel;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RuleAdapter extends RecyclerView.Adapter<RuleAdapter.RuleViewHolder> {

    public interface OnRuleClickListener {
        void onRuleClick(Long ruleId);
    }

    private final OnRuleClickListener listener;
    private List<RuleWithApps> rules;
    private Set<Long> toggledRuleIds = new HashSet<>();
    private Context context;

    public RuleAdapter(OnRuleClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
        return new RuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RuleViewHolder holder, int position) {
        RuleWithApps ruleWithApps = rules.get(position);
        holder.bind(ruleWithApps);
    }


    private static final DiffUtil.ItemCallback<Rule> DIFF_CALLBACK = new DiffUtil.ItemCallback<Rule>() {
        @Override
        public boolean areItemsTheSame(@NonNull Rule oldItem, @NonNull Rule newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Rule oldItem, @NonNull Rule newItem) {
            return Objects.equals(oldItem.ruleName, newItem.ruleName) && oldItem.deepSleepMode== newItem.deepSleepMode && oldItem.isEnabled == newItem.isEnabled && Objects.equals(oldItem.ruleDescription, newItem.ruleDescription);
        }
    };


    @Override
    public int getItemCount() {
        return rules != null ? rules.size() : 0;
    }

    public void setRules(List<RuleWithApps> rules) {
        this.rules = rules;
        notifyDataSetChanged();
    }


    public void setToggledRuleIds(Set<Long> toggledRuleIds) {
        this.toggledRuleIds = toggledRuleIds != null ? toggledRuleIds : new HashSet<>();
        notifyDataSetChanged();
    }

    class RuleViewHolder extends RecyclerView.ViewHolder {
        TextView ruleTitle;
        TextView ruleDescription;
        TextView ruleDeepSleepMode;
        MaterialSwitch ruleSwitch;

        public RuleViewHolder(@NonNull View itemView) {
            super(itemView);
            ruleTitle = itemView.findViewById(R.id.listItemTitle);
            ruleSwitch = itemView.findViewById(R.id.switch1);
            ruleDescription = itemView.findViewById(R.id.listItemDetails);
            ruleDeepSleepMode = itemView.findViewById(R.id.listItemDeepSleepOption);
        }

        public void bind(RuleWithApps ruleWithApps) {

            Rule rule = ruleWithApps.rule;
            boolean isToggled = toggledRuleIds.contains(rule.id);

            ruleTitle.setText((rule.ruleName == null || rule.ruleName.isEmpty()) ? "Rule #" + rule.id : rule.ruleName);


            if(rule.ruleDescription==null|| rule.ruleDescription.isEmpty())
            {
                ruleDescription.setText("No Apps selected, the rule will not work");

                ruleDescription.setTextColor( ContextCompat.getColor(context, design_default_color_error));
            }
            else
            {

                ruleDescription.setText(rule.ruleDescription);
                ruleDescription.setTextColor( ContextCompat.getColor(context, R.color.teal_200));


            }

            ruleDeepSleepMode.setText(rule.deepSleepMode ? "Deep Sleep Mode Enabled" : "");
            ruleDeepSleepMode.setTextColor( ContextCompat.getColor(context, R.color.purple_700));


            // Set switch state
            ruleSwitch.setOnCheckedChangeListener(null);
            ruleSwitch.setChecked(isToggled);
            ruleSwitch.setOnCheckedChangeListener((buttonView, checked) -> {
                if (checked) {
                    rule.isEnabled = true;

                    toggledRuleIds.add(rule.id);
                } else {
                    rule.isEnabled = false;

                    toggledRuleIds.remove(rule.id);
                }
                new Thread(()->{
                    MainActivity.db.ruleDao().updateRule(rule);
                }).start();
                notifyItemChanged(getAdapterPosition());
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onRuleClick(rule.id);
            });
        }
    }
}
