package com.etgames.alarmme;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class RuleWithApps {
    @Embedded
    public Rule rule;

    @Relation(
            parentColumn = "id",
            entityColumn = "ruleId"
    )
    public List<ToggledApp> toggledApps;
}
