package com.etgames.alarmme.ui.rules;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.etgames.alarmme.Helperr;
import com.etgames.alarmme.MainActivity;
import com.etgames.alarmme.R;
import com.etgames.alarmme.Rule;
import com.etgames.alarmme.RuleDao;
import com.etgames.alarmme.RuleWithApps;
import com.etgames.alarmme.SelectedRule;
import com.etgames.alarmme.SingleLiveEvent;
import com.etgames.alarmme.ToggledApp;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RulesViewModel extends ViewModel {

    private final RuleDao ruleDao;
    private final MutableLiveData<String> mText = new MutableLiveData<>();
    private final MutableLiveData<List<RuleWithApps>> allRulesLiveData = new MutableLiveData<>();
    public SingleLiveEvent<List<ToggledApp>> openAppList = new SingleLiveEvent<>();
    public AlertDialog dialogg;
    public List<ToggledApp> tempToggledApps;
    public Set<String> tempToggleSet;
    public String tempRuleDescription;

    public RulesViewModel() {
        ruleDao = MainActivity.db.ruleDao();
        mText.setValue("No rules added, add one by clicking on the \"+\" symbol.");
        loadRulesFromDb();
        Log.d("infoo", "RulesViewModel created");
    }

    // ---------------- LiveData getters ----------------
    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<List<RuleWithApps>> getAllRulesWithApps() {
        return allRulesLiveData;
    }

    // ---------------- Load rules from Room ----------------
    public void loadRulesFromDb() {
        new Thread(() -> {
            List<RuleWithApps> rules = ruleDao.getAllRulesWithApps();
            allRulesLiveData.postValue(rules);
            if (rules.isEmpty()) {
                mText.postValue("No rules added, add one by clicking on the \"+\" symbol.");
            } else {
                mText.postValue(null);
            }
        }).start();
    }


    public void deleteRule(long ruleId) {
        new Thread(() -> {
            ruleDao.deleteRuleById(ruleId);
            loadRulesFromDb();
        }).start();
    }

    // ---------------- Full input dialog for Room ----------------
    public void showInputDialog(Context context, Long ruleId, boolean newRule) {
        tempToggleSet = new HashSet<>();
        tempToggledApps = new ArrayList<>();
        tempRuleDescription = null;
        if (!newRule) {

            new Thread(() -> {
                MainActivity.db.runInTransaction(() -> {

                    List<String> toggledApps = MainActivity.db.ruleDao().getToggledAppsForRule(ruleId);
                    tempToggleSet = new HashSet<>(toggledApps);
                    RuleWithApps _rulewithApps = MainActivity.db.ruleDao().getRuleWithApps(ruleId);
                    if (_rulewithApps != null && _rulewithApps.toggledApps != null) {
                        tempToggledApps = _rulewithApps.toggledApps;
                    } else {
                        tempToggledApps = new ArrayList<>();
                    }
                });

            }).start();
        }

        new Thread(() -> {


                if (newRule) {
                    long lastRuleId;

                    //this is a workaround for knowing the next id that the roomdatabase will assign, since no API for that
                    Rule dummy = new Rule();
                    long insertedId = ruleDao.insertRule(dummy);
                    dummy.id = insertedId;
                    ruleDao.deleteRule(dummy);

                    lastRuleId = insertedId + 1;

                    MainActivity.db.selectedRuleDao().setSelectedRule(new SelectedRule(1, lastRuleId));

                    // saveRuleToDatabase(lastRuleId, editRuleName, isAndSwitch, isDeepSleepSwitch, senderNameTexts, commandsTexts, ruleDao);

                    Log.d("infoo", "Passed ruleId: " + lastRuleId);
                }


        }).start();


        View dialogView = LayoutInflater.from(context).inflate(R.layout.details_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        EditText editRuleName = dialogView.findViewById(R.id.editRuleName);
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
        com.google.android.material.materialswitch.MaterialSwitch isDeepSleepSwitch = dialogView.findViewById(R.id.isAnd0);

        ArrayList<TextInputLayout> senderNameTexts = new ArrayList<>();
        ArrayList<TextInputLayout> commandsTexts = new ArrayList<>();
        Helperr senderHelper = new Helperr(btnAddMore1, btnDeleteLast1, 5);
        Helperr commandHelper = new Helperr(btnAddMore2, btnDeleteLast2, 5);

        if (newRule) {
            btnDelete.setEnabled(false);
        } else {
            btnDelete.setOnClickListener(v -> {
                deleteRule(ruleId);
                dialog.dismiss();
            });
        }

        btnAddMore1.setOnClickListener(v -> senderHelper.addOne(context, senderNameTexts, senderContainer, "Notification Name", null));
        btnAddMore2.setOnClickListener(v -> commandHelper.addOne(context, commandsTexts, commandContainer, "Notification Content", null));
        btnDeleteLast1.setOnClickListener(v -> senderHelper.removeOne(senderNameTexts, senderContainer));
        btnDeleteLast2.setOnClickListener(v -> commandHelper.removeOne(commandsTexts, commandContainer));

        // Load rule from DB if editing
        new Thread(() -> {
            Rule currentRule = null;
            if (!newRule) {
                currentRule = ruleDao.getRuleById(ruleId);
            }

            Rule finalCurrentRule = currentRule;
            dialogView.post(() -> {
                if (finalCurrentRule != null) {
                    editRuleName.setText(finalCurrentRule.ruleName);
                    isAndSwitch.setChecked(finalCurrentRule.isAnd);
                    isDeepSleepSwitch.setChecked(finalCurrentRule.deepSleepMode);

                    for (String senderName : finalCurrentRule.preferedSenderName) {
                        senderHelper.addOne(context, senderNameTexts, senderContainer, "Notification Name", senderName);
                    }
                    for (String commandMessage : finalCurrentRule.preferedContentCommands) {
                        commandHelper.addOne(context, commandsTexts, commandContainer, "Notification Content", commandMessage);
                    }
                    tempRuleDescription = finalCurrentRule.ruleDescription;
                } else {
                    senderHelper.addOne(context, senderNameTexts, senderContainer, "Notification Name", null);
                    commandHelper.addOne(context, commandsTexts, commandContainer, "Notification Content", null);
                }
            });
        }).start();


        Button btnOpenAppList = dialogView.findViewById(R.id.BtnPref);
        btnOpenAppList.setEnabled(false); // initially disabled

        btnOpenAppList.setOnClickListener(v -> {

            this.dialogg = dialog; // save reference
            dialog.hide();          // hide current dialog

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            openAppList.setValue(tempToggledApps);// trigger the LiveData event
        });

// Enable button if MainActivity is loaded
        btnOpenAppList.setEnabled(MainActivity.isLoaded);
        // Save rule
        btnOk.setOnClickListener(v -> {

            new Thread(() -> {
                long _ruleId = MainActivity.db.selectedRuleDao().getSelectedRuleId();

                saveRuleToDatabase(_ruleId, editRuleName, isAndSwitch, isDeepSleepSwitch, senderNameTexts, commandsTexts, ruleDao);//this ssaved the toggled apps too


            }).start();
            // Dismiss dialog on UI thread
            dialog.dismiss();
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    /**
     * this saves the toggled apps too
     */
    private void saveRuleToDatabase(long ruleId, EditText editRuleName,
                                    com.google.android.material.materialswitch.MaterialSwitch isAndSwitch, com.google.android.material.materialswitch.MaterialSwitch isDeepSleepSwitch,
                                    List<TextInputLayout> senderNameTexts, List<TextInputLayout> commandsTexts,
                                    RuleDao ruleDao) {


        new Thread(() -> {
            boolean newRule = !MainActivity.db.ruleDao().doesRuleExist(ruleId);
            Log.w("infoo", "the newRule value is " + newRule);
            Rule ruleToSave;

            if (newRule) {
                ruleToSave = new Rule();
            } else {
                ruleToSave = ruleDao.getRuleById(ruleId);
                if (ruleToSave == null) ruleToSave = new Rule();
            }

            // Must extract UI values on main thread
            Handler mainHandler = new Handler(Looper.getMainLooper());
            Rule finalRuleToSave = ruleToSave;
            mainHandler.post(() -> {
                String ruleName = editRuleName.getText().toString().trim();
                boolean isAnd = isAndSwitch.isChecked();
                boolean deepSleep = isDeepSleepSwitch.isChecked();

                ArrayList<String> senderTexts = new ArrayList<>();
                for (TextInputLayout senderLayout : senderNameTexts) {
                    String text = senderLayout.getEditText().getText().toString().trim();
                    if (!text.isEmpty()) senderTexts.add(text);
                }

                ArrayList<String> commandTexts = new ArrayList<>();
                for (TextInputLayout commandLayout : commandsTexts) {
                    String text = commandLayout.getEditText().getText().toString().trim();
                    if (!text.isEmpty()) commandTexts.add(text);
                }

                // Move back to background thread to write to DB
                new Thread(() -> {
                    finalRuleToSave.ruleName = ruleName;
                    finalRuleToSave.isAnd = isAnd;
                    finalRuleToSave.deepSleepMode = deepSleep;
                    finalRuleToSave.preferedSenderName = new HashSet<>(senderTexts);
                    finalRuleToSave.preferedContentCommands = new HashSet<>(commandTexts);
                    finalRuleToSave.ruleDescription = tempRuleDescription;

                    if (newRule) {
                        MainActivity.db.runInTransaction(() -> {
                            long insertedID = MainActivity.db.ruleDao().insertRule(finalRuleToSave);
                            MainActivity.db.selectedRuleDao().setSelectedRule(new SelectedRule(1, insertedID));

                            MainActivity.db.ruleDao().replaceToggledAppsForRule(insertedID, tempToggledApps);
                        });
                    } else {
                        ruleDao.updateRule(finalRuleToSave);
                        MainActivity.db.ruleDao().replaceToggledAppsForRule(ruleId, tempToggledApps);
                    }

                    loadRulesFromDb();
                    ///TODO solve the problem that the toggled app list be auto saved wihtout user confermation even if pressed cancel
                    //  and also rememebr that u dissabled the save of the rule when pressing the open app list button
                    ///    and
                }).start();
            });

        }).start();
    }

}
