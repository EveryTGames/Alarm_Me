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
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.etgames.alarmme.Helperr;
import com.etgames.alarmme.MainActivity;
import com.etgames.alarmme.Rule;
import com.etgames.alarmme.RuleDao;
import com.etgames.alarmme.RuleWithApps;
import com.etgames.alarmme.SelectedRule;
import com.etgames.alarmme.SingleLiveEvent;
import com.etgames.alarmme.R;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RulesViewModel extends ViewModel {

    private final RuleDao ruleDao;
    public SingleLiveEvent<Void> openAppList = new SingleLiveEvent<>();
    private final MutableLiveData<String> mText = new MutableLiveData<>();
    private final MutableLiveData<List<RuleWithApps>> allRulesLiveData = new MutableLiveData<>();
    public AlertDialog dialogg;

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

    // ---------------- CRUD Operations ----------------
    public void addRule(String ruleName, boolean isEnabled, List<String> toggledApps) {
        new Thread(() -> {
            Rule newRule = new Rule();
            newRule.ruleName = ruleName;
            newRule.isEnabled = isEnabled;
            long ruleId = ruleDao.insertRule(newRule);

            List<com.etgames.alarmme.ToggledApp> apps = new ArrayList<>();
            for (String app : toggledApps) {
                apps.add(new com.etgames.alarmme.ToggledApp(ruleId, app));
            }
            ruleDao.insertToggledApps(apps);
            loadRulesFromDb();
        }).start();
    }

    public void updateRule(Rule rule) {
        new Thread(() -> {
            ruleDao.updateRule(rule);
            loadRulesFromDb();
        }).start();
    }

    public void addAppToRule(long ruleId, String appName) {
        new Thread(() -> {
            ruleDao.addAppToRule(ruleId, appName);
            loadRulesFromDb();
        }).start();
    }

    public void removeAppFromRule(long ruleId, String appName) {
        new Thread(() -> {
            ruleDao.removeAppFromRule(ruleId, appName);
            loadRulesFromDb();
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
            new Thread(() -> {
                long lastRuleId;
                if (newRule) {

                    lastRuleId = MainActivity.db.ruleDao().getLastRuleId() + 1;
                } else {
                    lastRuleId = MainActivity.db.selectedRuleDao().getSelectedRuleId();

                }

                if (lastRuleId == 0) {
                    lastRuleId = 1;
                    Log.e("infoo","i changed it from zero to 1, in RulesViewModel");
                }

                MainActivity.db.selectedRuleDao().setSelectedRule(new SelectedRule(1, lastRuleId));
                saveRuleToDatabase(lastRuleId, editRuleName, isAndSwitch, isDeepSleepSwitch, senderNameTexts, commandsTexts, ruleDao);
                Log.d("RULE_CLICK", "Passed ruleId: " + lastRuleId);
            }).start();
            openAppList.call();     // trigger the LiveData event
        });

// Enable button if MainActivity is loaded
        btnOpenAppList.setEnabled(MainActivity.isLoaded);
        // Save rule
        btnOk.setOnClickListener(v -> {

            new Thread(() -> {
                long _ruleId = MainActivity.db.selectedRuleDao().getSelectedRuleId();
                saveRuleToDatabase(_ruleId, editRuleName, isAndSwitch, isDeepSleepSwitch, senderNameTexts, commandsTexts, ruleDao);

            }).start();
            // Dismiss dialog on UI thread
            dialog.dismiss();
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

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

                    if (newRule) {
                        MainActivity.db.selectedRuleDao().setSelectedRule(new SelectedRule(1, ruleDao.insertRule(finalRuleToSave)));
                    } else {
                        ruleDao.updateRule(finalRuleToSave);
                    }

                    loadRulesFromDb(); // Assuming this is safe to call from background
                }).start();
            });

        }).start();
    }

}
