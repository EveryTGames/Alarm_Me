package com.etgames.alarmme;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "selected_rule")
public class SelectedRule {
    @PrimaryKey
    public int id = 1; // always only one row
    public long ruleId;

    public SelectedRule(int id, Long ruleId)
    {
        this.id=1;
        this.ruleId= ruleId;
    }
}
