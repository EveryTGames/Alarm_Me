package com.etgames.alarmme;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
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

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class appListAdapter extends ListAdapter<app_info, appListAdapter.viewHolder> {

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
    private final ToggledAppsCallback callback;
    private Context context;
    private long currentRuleId; // The rule currently being edited
    private Set<String> toggleSet = new HashSet<>(); // apps toggled for current rule
    private List<ToggledApp> tempToggledApps = new ArrayList<>();

    public appListAdapter(Context _context, long ruleId,List<ToggledApp> _tempToggledApps, ToggledAppsCallback callBack) {
        super(DIFF_CALLBACK);
        context = _context;
        currentRuleId = ruleId;
        tempToggledApps=_tempToggledApps;

        this.callback = callBack;
       // loadToggledAppsFromDb();
    }

    private void loadToggledAppsFromDb() {


        new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());
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

    public interface ToggledAppsCallback {
        void onToggledAppsChanged(List<ToggledApp> toggledApps, Set<String> toggledSet);
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

                    MainActivity.db.runInTransaction(() -> {
                        currentRuleId = MainActivity.db.selectedRuleDao().getSelectedRuleId();
                        if (isChecked) {
                            // Insert ToggledApp safely
                            tempToggledApps.add(new ToggledApp(currentRuleId, itemData.packageName));
                            toggleSet.add(itemData.packageName);
                        } else {
                          boolean result =  tempToggledApps.remove(new ToggledApp(currentRuleId, itemData.packageName));
                            Log.d("infoo","removing from the toglled apps is " + result);
                            toggleSet.remove(itemData.packageName);
                        }
                    });
                    // Notify UI on main thread
                    new Handler(Looper.getMainLooper()).post(() -> notifyItemChanged(getAdapterPosition()));
                }).start();

                if (callback != null) {
                    callback.onToggledAppsChanged(tempToggledApps,toggleSet);
                }
            });

        }
    }
}
