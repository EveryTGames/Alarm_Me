package com.etgames.alarmme;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.Objects;
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
            return oldItem.Description.equals( newItem.Description) && oldItem.Title.equals( newItem.Title) && oldItem.isEnabled == newItem.isEnabled && oldItem.isDeepSleepMode == newItem.isDeepSleepMode && oldItem.backGroundColor == newItem.backGroundColor && Objects.equals(oldItem.photoUri, newItem.photoUri);
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
        ImageView  colorView;
        ImageView alarmPhoto;
        com.google.android.material.materialswitch.MaterialSwitch alarmSwitch;

        public AlarmsViewHolder(@NonNull View itemView) {
            super(itemView);
            alarmTitle = itemView.findViewById(R.id.listItemTitle);
            alarmSwitch = itemView.findViewById(R.id.switch1);
            alarmDescription = itemView.findViewById(R.id.listItemDetails);
            alarmPhoto = itemView.findViewById(R.id.appIcon);
            ruleDeepSleepMode = itemView.findViewById(R.id.listItemDeepSleepOption);
            colorView = itemView.findViewById(R.id.colorView);

        }


        public void bind(Alarm alarm) {
            boolean isToggled = toggleSet.contains( alarm.id);
            alarmTitle.setText(alarm.Title);
            alarmDescription.setText(alarm.Description);
            if (alarm.photoUri != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(alarm.photoUri);
                alarmPhoto.setImageBitmap(bitmap);
                alarmPhoto.setVisibility(View.VISIBLE);
            } else {
                alarmPhoto.setImageResource(0);
                alarmPhoto.setVisibility(View.GONE);
               Log.w("infoo", "No image URI to load");
            }
            alarmSwitch.setOnCheckedChangeListener(null);
            alarmSwitch.setChecked(isToggled);
            alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

                if (isChecked) {
                    toggleSet.add(alarm.id);



                } else {
                    toggleSet.remove(alarm.id);


               }

                if (listener != null) {
                    listener.onAlarmToggle(alarm, isChecked); //  Notify ViewModel
                }
            });

            ruleDeepSleepMode.setText(alarm.isDeepSleepMode?"Deep Sleep Mode is Enabled":"" );
            colorView.setVisibility(View.VISIBLE);
            colorView.setBackgroundColor(alarm.backGroundColor);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAlarmClick(alarm);
            });


        }
    }


    private Set<Long> toggleSet = new HashSet<>();

    public void setToggleState(Set<Long> toggleSet) {
        this.toggleSet = toggleSet != null ? toggleSet : new HashSet<>();
        notifyDataSetChanged(); // refresh all views with new toggle state
    }
}
