package com.etgames.alarmme;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

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
    protected appListAdapter(Context _context) {
        super(DIFF_CallBack);
        context = _context;
    }

    //the class responsible for managing the view of each list item
    public static class viewHolder extends RecyclerView.ViewHolder {

        TextView appName;
        ImageView appIcon;
        com.google.android.material.materialswitch.MaterialSwitch switchToggle;
        appListAdapter _adapter;

        //the constructor for this class :)
        public viewHolder(@NonNull View itemView, appListAdapter _adapter) {
            super(itemView);
            appName = itemView.findViewById(R.id.appName);
            appIcon = itemView.findViewById(R.id.appIcon);
            switchToggle = itemView.findViewById(R.id.switch1);
            this._adapter = _adapter;
        }

        // a function to be used in onbind function, just to make things more organised
        void bind(app_info itemData) {
            appName.setText(itemData.appName);
            appIcon.setImageDrawable(itemData.appIcon);
            switchToggle.setOnCheckedChangeListener(null);
            switchToggle.setChecked(itemData.isToggled);
            switchToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                itemData.isToggled = isChecked;
                if (isChecked) {
                    MainActivity.toggledApps.add(itemData.packageName);
                    _adapter.showInputDialog(_adapter.context, itemData.packageName, switchToggle);
                } else {
                    MainActivity.toggledApps.remove(itemData.packageName);
                    //u will also make the logivc for saving the texts like the prefered command and prefered name
                    // u will use shared preference normally
                    // key: "package name"    value : "<preferered sender name><"or"  or "and" to determine if the user wants the sender name and one of the content messages to match in order to trigger the alarm, or just want any one of them>:#94710341#:<prefered content message 1>:#94710341#:<prefered content message 2 ... and so on>"
                }
                MainActivity.saveSet();
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


    @Override
    //it is called when assigning new data for recycled or new viewholder
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        holder.bind(getItem(position));
    }


    void showInputDialog(Context context, String packageName, com.google.android.material.materialswitch.MaterialSwitch srcSwitch) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.details_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); //to not disappear when the user miss-click on the outside (i hated it so much when happens to me XD )

        Button btnOk = dialogView.findViewById(R.id.btnOk);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnAddMore1 = dialogView.findViewById(R.id.btnAddMore);
        Button btnAddMore2 = dialogView.findViewById(R.id.btnAddMore2);
        Button btnDeleteLast1 = dialogView.findViewById(R.id.btnDeleteLast);
        Button btnDeleteLast2 = dialogView.findViewById(R.id.btnDeleteLast2);
        //   EditText baseEdit1 = dialogView.findViewById(R.id.editTextInput);
        // EditText baseEdit2 = dialogView.findViewById(R.id.editTextInput2);
        LinearLayout senderContainer = dialogView.findViewById(R.id.senderTexts);
        LinearLayout commandContainer = dialogView.findViewById(R.id.comandTexts);
        com.google.android.material.materialswitch.MaterialSwitch isAndSwitch = dialogView.findViewById(R.id.isAnd);
        com.google.android.material.materialswitch.MaterialSwitch isdeepsleepSwitch = dialogView.findViewById(R.id.isAnd0);
        ArrayList<EditText> senderNameTexts = new ArrayList<>();
        ArrayList<EditText> commmandsTexts = new ArrayList<>();
        Helper command = new Helper(btnAddMore2, btnDeleteLast2);
        Helper Sender = new Helper(btnAddMore1, btnDeleteLast1);


        PackageDetails pd = PackageDetails.retrieve(packageName);
        if (pd == null) {

            Sender.addOne(context, senderNameTexts, senderContainer, null);

            command.addOne(context, commmandsTexts, commandContainer, null);

        } else {
            Log.d("infoo", Arrays.toString(pd.preferedContentCommands));

            for (String senderName :
                    pd.preferedSenderName) {
                Sender.addOne(context, senderNameTexts, senderContainer, senderName);

            }

            isAndSwitch.setChecked(pd.isAnd);
            isdeepsleepSwitch.setChecked(pd.deepSleepMode);

            for (String commandMessage :
                    pd.preferedContentCommands) {
                command.addOne(context, commmandsTexts, commandContainer, commandMessage);

            }

        }

        btnAddMore1.setOnClickListener(v ->
                Sender.addOne(context, senderNameTexts, senderContainer, null));


        btnAddMore2.setOnClickListener(v ->
                command.addOne(context, commmandsTexts, commandContainer, null));

        btnDeleteLast1.setOnClickListener(v ->
                Sender.removeOne(senderNameTexts, senderContainer));
        btnDeleteLast2.setOnClickListener(v ->
                command.removeOne(commmandsTexts, commandContainer));

        btnOk.setOnClickListener(v ->
        {
            StringBuilder packagDetails = new StringBuilder();
            for (EditText SenderName :
                    senderNameTexts) {
                String text = SenderName.getText().toString().trim();
                if (!text.isEmpty()) {
                    packagDetails.append(text);
                    packagDetails.append(MainActivity.ENCODE_SPLIT_SUBs);
                }
            }

            packagDetails.append(MainActivity.ENCODE_SPLIT_MAIN);
            packagDetails.append(isAndSwitch.isChecked() ? "and" : "or");
            packagDetails.append(MainActivity.ENCODE_SPLIT_MAIN);

            for (EditText commandText :
                    commmandsTexts) {
                String text = commandText.getText().toString().trim();
                if (!text.isEmpty()) {
                    packagDetails.append(text);
                    packagDetails.append(MainActivity.ENCODE_SPLIT_SUBs);
                }
            }


            packagDetails.append(MainActivity.ENCODE_SPLIT_MAIN);
            if(isdeepsleepSwitch.isChecked())
            {
                packagDetails.append("on");
            }
            else
            {
                packagDetails.append("off");
            }

            Log.d("infoo",packagDetails.toString());

            MainActivity.prefs.edit().putString(packageName, packagDetails.toString()).apply();


            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> {
                    srcSwitch.setChecked(false);
                    dialog.dismiss();
                }
        );

        dialog.show();
    }


}

//this class is a workaround to use non final variable inside the lambda expression
//it helped organising the code tho :)
class Helper {
    EditText last;
    int lastIndex;
    Button btnAdd;
    Button btnRemove;

    public Helper(Button _btnAdd, Button _btnRemove) {
        btnAdd = _btnAdd;
        btnRemove = _btnRemove;
    }


    EditText creatEditText(Context context, String data) {
        EditText newEditText = new EditText(context);
        newEditText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        newEditText.setHint("add a new one");
        newEditText.setText(data);
        return newEditText;
    }

    public void addOne(Context context, ArrayList<EditText> TextsList, LinearLayout container, String data) {
        boolean firstOne = false;
        if (TextsList.isEmpty()) {

            last = creatEditText(context, data);
            container.addView(last);
            TextsList.add(last);
            lastIndex = 0;
            firstOne = true;
            btnRemove.setEnabled(false);
            if (data != null) {
                return;
            }
        }
        last = TextsList.get(lastIndex);
        if (last.getText().toString().trim().isEmpty()) {
            if (!firstOne) {

                last.setError("can't be empty");
            }
            return;
        }
        last.setEnabled(false);
        change(1);
        last = creatEditText(context, data);
        container.addView(last);
        TextsList.add(last);
    }

    public void removeOne(ArrayList<EditText> TextsList, LinearLayout container) {
        if (lastIndex > 0) {

            TextsList.remove(last);
            container.removeView(last);
            change(-1);
            last = TextsList.get(lastIndex);
            last.setEnabled(true);
        }
    }

    public void change(int value) {
        lastIndex += value;

        switch (lastIndex) {
            case 0:
                btnRemove.setEnabled(false);
                break;
            case 1:
                btnRemove.setEnabled(true);
                break;
            case 4:
                btnAdd.setEnabled(true);
                break;
            case 5:
                btnAdd.setEnabled(false);
                break;
            default:
                break;
        }


    }
}


