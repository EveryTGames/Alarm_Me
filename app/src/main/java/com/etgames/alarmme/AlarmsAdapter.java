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


public class AlarmsAdapter extends ListAdapter<Alarm, AlarmsAdapter.AlarmsViewHolder> {

    public interface OnAlarmClickListener {
        void onAlarmClick(Alarm alarm);
        void onAlarmToggle(Alarm alarm, boolean isEnabled); // for switch toggle
    }

    private final OnAlarmClickListener listener;

    public AlarmsAdapter(OnAlarmClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Alarm> DIFF_CALLBACK = new DiffUtil.ItemCallback<Alarm>() {
        @Override
        public boolean areItemsTheSame(@NonNull Alarm oldItem, @NonNull Alarm newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Alarm oldItem, @NonNull Alarm newItem) {
            return oldItem.Description.equals( newItem.Description) && oldItem.Title.equals( newItem.Title) && oldItem.isEnabled == newItem.isEnabled && oldItem.isDeepSleepMode == newItem.isDeepSleepMode;
        }
    };

    @NonNull
    @Override
    public AlarmsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
        return new AlarmsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmsViewHolder holder, int position) {
        Alarm alarm = getItem(position);
        holder.bind(alarm);
    }

    class AlarmsViewHolder extends RecyclerView.ViewHolder {
        TextView alarmTitle;
        TextView alarmDescription;
        TextView ruleDeepSleepMode;
        com.google.android.material.materialswitch.MaterialSwitch alarmSwitch;

        public AlarmsViewHolder(@NonNull View itemView) {
            super(itemView);
            alarmTitle = itemView.findViewById(R.id.listItemTitle);
            alarmSwitch = itemView.findViewById(R.id.switch1);
            alarmDescription = itemView.findViewById(R.id.listItemDetails);
            ruleDeepSleepMode = itemView.findViewById(R.id.listItemDeepSleepOption);

        }


        public void bind(Alarm alarm) {
            boolean isToggled = toggleSet.contains(Long.toString( alarm.id));
            alarmTitle.setText(alarm.Title);
            alarmDescription.setText(alarm.Description);
            alarmSwitch.setOnCheckedChangeListener(null);
            alarmSwitch.setChecked(isToggled);
            alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

                if (isChecked) {
                    toggleSet.add(Long.toString(alarm.id));
                    MainActivity.prefs.edit().putStringSet("toggledAlarms", toggleSet).apply();


                } else {
                    toggleSet.remove(Long.toString(alarm.id));

                    MainActivity.prefs.edit().putStringSet("toggledAlarms", toggleSet).apply();
                    //u will also make the logivc for saving the texts like the prefered command and prefered name
                    // u will use shared preference normally
                    // key: "package name"    value : "<preferered sender name><"or"  or "and" to determine if the user wants the sender name and one of the content messages to match in order to trigger the alarm, or just want any one of them>:#94710341#:<prefered content message 1>:#94710341#:<prefered content message 2 ... and so on>"
                }

                if (listener != null) {
                    listener.onAlarmToggle(alarm, isChecked); // 🧠 Notify ViewModel
                }
            });

            ruleDeepSleepMode.setText(alarm.isDeepSleepMode?"Deep Sleep Mode is Enabled":"" );


            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAlarmClick(alarm);
            });

        }
    }


    private Set<String> toggleSet = new HashSet<>();

    public void setToggleState(Set<String> toggleSet) {
        this.toggleSet = toggleSet != null ? toggleSet : new HashSet<>();
        notifyDataSetChanged(); // refresh all views with new toggle state
    }
}
