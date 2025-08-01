package com.etgames.alarmme.ui.rules;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

//import com.etgames.alarmme.Helperr;
import com.etgames.alarmme.Helperr;
import com.etgames.alarmme.MainActivity;
import com.etgames.alarmme.PackageDetailss;
import com.etgames.alarmme.R;
import com.etgames.alarmme.SingleLiveEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RulesViewModel extends ViewModel {
    public SingleLiveEvent<Void> openAppList = new SingleLiveEvent<>();


    private final MutableLiveData<Pair<List<String>, HashSet<String>>> allRulesLiveData = new MutableLiveData<>();

    public LiveData<Pair<List<String>, HashSet<String>>> getAllRules() {
        return allRulesLiveData;
    }

    private final MutableLiveData<String> mText = new MutableLiveData<>();



    public void loadRules() {
        Set<String> rulesSet = MainActivity.prefs.getStringSet("allRules", new HashSet<>());
        Set<String> setToggledSet = MainActivity.prefs.getStringSet("toggledRules", new HashSet<>());

        allRulesLiveData.setValue(new Pair<List<String>, HashSet<String>>(new ArrayList<>(rulesSet), new HashSet<>(setToggledSet)));
        if ( rulesSet.isEmpty()) {
            mText.setValue("no rules added, add one by clicking on the \"+\" symbol.");
        } else {
            mText.setValue(null); // You can also use null or just leave it empty to hide it
        }
    }
    public LiveData<String> getText() {
        return mText;
    }

    public AlertDialog dialogg;

    public RulesViewModel() {
        mText.setValue("no rules added, add one by clicking on the \"+\" symbol.");


        loadRules();
        Log.d("infoo", "created the constructor");
    }


    public static void saveToggledRules(Set<String> toggledRules) {
        MainActivity.prefs.edit().putStringSet("toggledRules", new HashSet<>(toggledRules)).apply();
    }


    //TODO
    // known issues is when when i change the fragment to something else and be back the recycle view be empty
    // to solve it seet he last thing in chatgpt


    public void addRuleId(String newRuleId) {
        SharedPreferences prefs = MainActivity.prefs;

        Set<String> currentSet = prefs.getStringSet("allRules", new HashSet<>());


        Set<String> updatedSet = new HashSet<>(currentSet);

        updatedSet.add(newRuleId);

        prefs.edit().putStringSet("allRules", updatedSet).apply();

        loadRules();
    }

    public void removeRuleId(String ruleId) {
        SharedPreferences prefs = MainActivity.prefs;

        Set<String> currentSet = prefs.getStringSet("allRules", new HashSet<>());
        Set<String> toggledSet = prefs.getStringSet("toggledRules", new HashSet<>());
        Set<String> toggledAppsForThisRule = MainActivity.getToggledAppsForRule(ruleId);
        Set<String> ToggledApps = prefs.getStringSet("toggled_apps", new HashSet<>());


        Set<String> updatedSet = new HashSet<>(currentSet);
        Set<String> updatedToggledSet = new HashSet<>(toggledSet);
        Set<String> updatedToggledApps = new HashSet<>(ToggledApps);

        updatedSet.remove(ruleId);
        updatedToggledSet.remove(ruleId);

        for (String packageName :
                toggledAppsForThisRule) {

            String IDs = prefs.getString(packageName, null);
            String newIDs = getNewPackageIDs(ruleId, IDs);
            if (newIDs == null) {

               updatedToggledApps.remove(packageName);//that means this package isnt applied to any rule anymore
            }
            else {

                MainActivity.prefs.edit().putString(packageName, newIDs).apply(); //i added it in else, and i know that even if it was null it will be removed instead, but just to make things clearer
            }


        }


        prefs.edit().putStringSet("allRules", updatedSet).apply();
        prefs.edit().putStringSet("toggledRules", updatedToggledSet).apply();
        prefs.edit().putStringSet("toggled_apps", updatedToggledApps).apply();
        prefs.edit().remove("toggled_apps_" + ruleId).apply();


        loadRules();
    }

    private static @Nullable String getNewPackageIDs(String ruleId, String IDs) {
        String[] IDsArray = {};
        String newIDs = null;
        if (IDs != null) {

            IDsArray = IDs.split(",");
        }

        StringBuilder IDsStringBuilder = new StringBuilder();
        for (String ID :
                IDsArray) {
            if (Objects.equals(ruleId, ID)) {
                continue;
            }
            if (IDsStringBuilder.length() > 0) {
                IDsStringBuilder.append(",");
            }
            IDsStringBuilder.append(ID);
            newIDs = IDsStringBuilder.toString();
        }
        return newIDs;
    }

    public void showInputDialog(Context context, String ID, boolean newRule) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.details_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); //to not disappear when the user miss-click on the outside (i hated it so much when happens to me XD )

        Button btnOk = dialogView.findViewById(R.id.btnOk);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        Button btnAddMore1 = dialogView.findViewById(R.id.btnAddMore);
        Button btnAddMore2 = dialogView.findViewById(R.id.btnAddMore2);
        Button btnDeleteLast1 = dialogView.findViewById(R.id.btnDeleteLast);
        Button btnDeleteLast2 = dialogView.findViewById(R.id.btnDeleteLast2);
        LinearLayout senderContainer = dialogView.findViewById(R.id.senderTexts);
        LinearLayout commandContainer = dialogView.findViewById(R.id.comandTexts);
        com.google.android.material.materialswitch.MaterialSwitch isAndSwitch = dialogView.findViewById(R.id.isAnd);
        com.google.android.material.materialswitch.MaterialSwitch isdeepsleepSwitch = dialogView.findViewById(R.id.isAnd0);
        ArrayList<EditText> senderNameTexts = new ArrayList<>();
        ArrayList<EditText> commmandsTexts = new ArrayList<>();
        Helperr command = new Helperr(btnAddMore2, btnDeleteLast2);
        Helperr Sender = new Helperr(btnAddMore1, btnDeleteLast1);

        MainActivity.currentID = ID;

        if (newRule) {
            btnDelete.setEnabled(false);
        } else {

            btnDelete.setOnClickListener(v -> {
                removeRuleId(ID);
                dialog.dismiss();

            });

        }

        Button btnOpenAppList = dialogView.findViewById(R.id.BtnPref);
        btnOpenAppList.setEnabled(false);
        btnOpenAppList.setOnClickListener(v -> {
            this.dialogg = dialog;
            dialog.hide();

            openAppList.call();
        });


        btnOpenAppList.setEnabled(MainActivity.isLoaded);


        PackageDetailss pd = new PackageDetailss(context);
        pd = pd.retrieve(ID);

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
            if (isdeepsleepSwitch.isChecked()) {
                packagDetails.append("on");
            } else {
                packagDetails.append("off");
            }

            Log.d("infoo", packagDetails.toString());

            MainActivity.prefs.edit().putString(ID, packagDetails.toString()).apply();
            if (newRule) {

                MainActivity.prefs.edit().putString("lastID", ID).apply();
                MainActivity.lastID += 1; // it now equals to ID that we saved in the preferences
                addRuleId(ID);
            }
            //TODO
            // you will need to add here the save of the toggled apps for this rule,
            // and also, to make the toggled apps fragment, to be entered with the id so that the toggled list for this id will be displayed
            // and a confirm button there so that when clicked it be returned to the rules fragment and the toggled apps for this id to be saved


            //TODO
            // the option to edit the toggled apps will be on each rule item in the rules fragment, not in the rule details

            //TODO
            // remember to after deleting a rule, the toggledApps for it be deleted
            // and the data of its id be deleted also,
            // and see if u made any new things that will be deleted too


            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> {
                    // srcSwitch.setChecked(false);


                    dialog.dismiss();
                }
        );

        dialog.show();
    }
}
