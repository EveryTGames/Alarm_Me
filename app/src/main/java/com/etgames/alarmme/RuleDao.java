package com.etgames.alarmme;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.Collections;
import java.util.List;

@Dao
public interface RuleDao {

    // ----- Rule CRUD -----
    @Insert
    long insertRule(Rule rule);

    @Update
    void updateRule(Rule rule);

    @Delete
    void deleteRule(Rule rule);

    @Query("SELECT * FROM rules WHERE id = :ruleId")
    Rule getRuleById(long ruleId);

    @Query("SELECT * FROM rules")
    List<Rule> getAllRules();



    // ----- ToggledApp CRUD -----
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertToggledApps(List<ToggledApp> apps);

    @Query("DELETE FROM toggled_apps WHERE ruleId = :ruleId")
    void deleteToggledAppsByRuleId(long ruleId);

    @Transaction
    default void replaceToggledAppsForRule(long ruleId, List<ToggledApp> newApps) {
        deleteToggledAppsByRuleId(ruleId);
        insertToggledApps(newApps);
    }

    @Delete
    void deleteToggledApps(List<ToggledApp> apps);


    @Insert
    void insertToggledApp(ToggledApp app); //  Add single ToggledApp

    @Delete
    void deleteToggledApp(ToggledApp app); //  Delete single ToggledApp


    @Query("SELECT EXISTS(SELECT 1 FROM rules WHERE id = :ruleId)")
    boolean doesRuleExist(long ruleId);

    @Query("SELECT DISTINCT appName FROM toggled_apps")
    List<String> getAllToggledApps(); // all apps across all rules

    @Query("SELECT appName FROM toggled_apps WHERE ruleId = :ruleId")
    List<String> getToggledAppsForRule(long ruleId); // apps for a specific rule

    // ----- Relation -----
    @Transaction
    @Query("SELECT * FROM rules WHERE id = :ruleId")
    RuleWithApps getRuleWithApps(long ruleId); // rule + its toggled apps

    @Query("SELECT EXISTS(SELECT 1 FROM toggled_apps WHERE appName = :packageName)")
    boolean isAppToggled(String packageName);

    @Transaction
    @Query("SELECT * FROM rules")
    List<RuleWithApps> getAllRulesWithApps(); // all rules + their toggled apps

    // Returns a list of rule IDs that have this app
    @Query("SELECT ruleId FROM toggled_apps WHERE appName = :appName")
    List<Long> getRuleIdsByAppName(String appName);

    @Query("DELETE FROM rules WHERE id = :ruleId")
    void deleteRuleById(long ruleId);


    @Query("SELECT MAX(id) FROM rules")
    long getLastRuleId();


    // Returns all rules where isEnabled = true
    @Query("SELECT id FROM rules WHERE isEnabled = 1")
    List<Long> getAllEnabledRules();

    @Transaction
    @Query("SELECT * FROM rules WHERE isEnabled = 1")
    List<RuleWithApps> getAllEnabledRulesWithApps();

    @Transaction
    default void addAppToRule(long ruleId, String appName) {
        insertToggledApps(Collections.singletonList(new ToggledApp(ruleId, appName)));
    }

    @Transaction
    default void removeAppFromRule(long ruleId, String appName) {
        deleteToggledApps(Collections.singletonList(new ToggledApp(ruleId, appName)));
    }

}