package com.etgames.alarmme;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class appListAdapter extends ListAdapter<app_info, appListAdapter.viewHolder> {

    private Context context;
    private long currentRuleId; // The rule currently being edited
    private Set<String> toggleSet = new HashSet<>(); // apps toggled for current rule

    // DiffUtil callback for list updates
    static final DiffUtil.ItemCallback<app_info> DIFF_CALLBACK = new DiffUtil.ItemCallback<app_info>() {
        @Override
        public boolean areItemsTheSame(@NonNull app_info oldItem, @NonNull app_info newItem) {
            return oldItem.packageName.equals(newItem.packageName);
        }

        @Override
        public boolean areContentsTheSame(@NonNull app_info oldItem, @NonNull app_info newItem) {
            return oldItem.isToggled == newItem.isToggled;
        }
    };

    public appListAdapter(Context _context, long ruleId) {
        super(DIFF_CALLBACK);
        context = _context;
        currentRuleId = ruleId;
        loadToggledAppsFromDb();
    }

    private void loadToggledAppsFromDb() {
        new Thread(() -> {
            List<String> toggledApps = MainActivity.db.ruleDao().getToggledAppsForRule(currentRuleId);
            toggleSet = new HashSet<>(toggledApps);
            notifyDataSetChanged();
        }).start();
    }

    public void updateCurrentRule(long ruleId) {
        currentRuleId = ruleId;
        loadToggledAppsFromDb();
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View createdView = LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false);
        return new viewHolder(createdView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public void setToggleState(Set<String> toggleSet) {
        this.toggleSet = toggleSet != null ? toggleSet : new HashSet<>();
        notifyDataSetChanged();
    }

    class viewHolder extends RecyclerView.ViewHolder {

        TextView itemTitle;
        ImageView appIcon;
        MaterialSwitch switchToggle;
        appListAdapter _adapter;

        public viewHolder(@NonNull View itemView, appListAdapter _adapter) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.listItemTitle);
            appIcon = itemView.findViewById(R.id.appIcon);
            switchToggle = itemView.findViewById(R.id.switch1);
            this._adapter = _adapter;
        }

        void bind(app_info itemData) {
            boolean isToggled = _adapter.toggleSet.contains(itemData.packageName);
            itemTitle.setText(itemData.itemTitle);
            Drawable icon = itemData.appIcon;
            if (icon != null) appIcon.setImageDrawable(icon);

            switchToggle.setOnCheckedChangeListener(null);
            switchToggle.setChecked(isToggled);
            switchToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                itemData.isToggled = isChecked;

                new Thread(() -> {
                    currentRuleId = MainActivity.db.selectedRuleDao().getSelectedRuleId();
                    if (isChecked) {
                        // Insert ToggledApp safely
                        MainActivity.db.ruleDao().insertToggledApp(new ToggledApp(currentRuleId, itemData.packageName));
                        toggleSet.add(itemData.packageName);
                    } else {
                        MainActivity.db.ruleDao().deleteToggledApp(new ToggledApp(currentRuleId, itemData.packageName));
                        toggleSet.remove(itemData.packageName);
                    }

                    // Notify UI on main thread
                    new Handler(Looper.getMainLooper()).post(() -> notifyItemChanged(getAdapterPosition()));
                }).start();
            });
        }
    }
}
