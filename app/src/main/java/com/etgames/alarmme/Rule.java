package com.etgames.alarmme;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.HashSet;
import java.util.Set;

@Entity(tableName = "rules")
public class Rule {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String ruleName;
    public Set<String> preferedSenderName = new HashSet<>();
    public boolean isAnd;
    public Set<String> preferedContentCommands = new HashSet<>();
    public boolean isEnabled;
    public boolean deepSleepMode;
    public String ruleDescription;
}