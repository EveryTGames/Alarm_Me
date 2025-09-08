package com.etgames.alarmme;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

@Dao
public interface SelectedRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void setSelectedRule(SelectedRule selectedRule);


        ///TODO if there are any issues arrised like forigny key things, or the new toggled apps has the wrong ID
        //  then just make a static variable and dont make this in the room database, bc it is slow to write updates
    @Query("SELECT ruleId FROM selected_rule WHERE id = 1")
    long getSelectedRuleId();
}
