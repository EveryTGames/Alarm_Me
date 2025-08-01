package com.etgames.alarmme;

import android.content.Context;
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

public class appListAdapter extends ListAdapter<app_info, appListAdapter.viewHolder> {

    //used for when the list adapter be updating the list
    static final DiffUtil.ItemCallback<app_info> DIFF_CallBack = new DiffUtil.ItemCallback<app_info>() {

        //obvious from the name :)
        @Override
        public boolean areItemsTheSame(@NonNull app_info oldItem, @NonNull app_info newItem) {
            return oldItem.packageName.equals(newItem.packageName);
        }

        //that's obvious too XD
        @Override
        public boolean areContentsTheSame(@NonNull app_info oldItem, @NonNull app_info newItem) {
            return oldItem.isToggled == newItem.isToggled;
        }
    };

    Context context;

    //constructor for this custom list adapter (will be used in main activity)
    public appListAdapter(Context _context) {
        super(DIFF_CallBack);
        context = _context;
    }

    //the class responsible for managing the view of each list item
    public static class viewHolder extends RecyclerView.ViewHolder {

        TextView itemTitle;
        ImageView appIcon;
        com.google.android.material.materialswitch.MaterialSwitch switchToggle;
        appListAdapter _adapter;

        //the constructor for this class :)
        public viewHolder(@NonNull View itemView, appListAdapter _adapter) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.listItemTitle);
            appIcon = itemView.findViewById(R.id.appIcon);
            switchToggle = itemView.findViewById(R.id.switch1);
            this._adapter = _adapter;
        }

        // a function to be used in onbind function, just to make things more organised
        void bind(app_info itemData) {
            boolean isToggled = _adapter.toggleSet.contains(itemData.packageName);
            itemTitle.setText(itemData.itemTitle);
            appIcon.setImageDrawable(itemData.appIcon);
            switchToggle.setOnCheckedChangeListener(null);
            switchToggle.setChecked(isToggled);
            switchToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                itemData.isToggled = isChecked;
                if (isChecked) {
                    _adapter.toggleSet.add(itemData.packageName);
                    MainActivity.toggledApps.add(itemData.packageName);
                    MainActivity.toggledAppsForCurrentRule.add(itemData.packageName);

                    String orignalIDs = MainActivity.prefs.getString(itemData.packageName, null);
                    MainActivity.prefs.edit().putString(itemData.packageName, (orignalIDs != null ? orignalIDs + "," : "") + MainActivity.currentID).apply();
                    Log.d("infoo",MainActivity.prefs.getString(itemData.packageName, null));

                } else {
                    _adapter.toggleSet.remove(itemData.packageName);
                    MainActivity.toggledAppsForCurrentRule.remove(itemData.packageName);
                    String IDs = MainActivity.prefs.getString(itemData.packageName, null);
                    String[] IDsArray = {};
                    String newIDs = null;
                    if (IDs != null) {

                        IDsArray = IDs.split(",");
                    }

                    StringBuilder IDsStringBuilder = new StringBuilder();
                    for (String ID :
                            IDsArray) {
                        if (Objects.equals(MainActivity.currentID, ID)) {
                            continue;
                        }
                        if (IDsStringBuilder.length() > 0) {
                            IDsStringBuilder.append(",");
                        }
                        IDsStringBuilder.append(ID);
                        newIDs = IDsStringBuilder.toString();
                    }
                    if (newIDs == null) {

                        MainActivity.toggledApps.remove(itemData.packageName);
                    }
                    MainActivity.prefs.edit().putString(itemData.packageName, newIDs).apply();
                    //u will also make the logivc for saving the texts like the prefered command and prefered name
                    // u will use shared preference normally
                    // key: "package name"    value : "<preferered sender name><"or"  or "and" to determine if the user wants the sender name and one of the content messages to match in order to trigger the alarm, or just want any one of them>:#94710341#:<prefered content message 1>:#94710341#:<prefered content message 2 ... and so on>"
                }
                MainActivity.saveSet();
                MainActivity.saveToggledAppsForRule(MainActivity.currentID, MainActivity.toggledAppsForCurrentRule);
            });

            // Log.d("infoo", "one item view created");
        }
    }

    @NonNull
    @Override
    //it is called when a brand new viewholder is created (not recycled)
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View createdView = LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false);
        return new viewHolder(createdView, this);
    }

    private Set<String> toggleSet = new HashSet<>();

    public void setToggleState(Set<String> toggleSet) {
        this.toggleSet = toggleSet != null ? toggleSet : new HashSet<>();
        notifyDataSetChanged(); // refresh all views with new toggle state
    }

    @Override
    //it is called when assigning new data for recycled or new viewholder
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        holder.bind(getItem(position));
    }


}


