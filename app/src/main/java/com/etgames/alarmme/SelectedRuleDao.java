package com.etgames.alarmme;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface SelectedRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void setSelectedRule(SelectedRule selectedRule);

    @Query("SELECT ruleId FROM selected_rule WHERE id = 1")
    long getSelectedRuleId();
}
