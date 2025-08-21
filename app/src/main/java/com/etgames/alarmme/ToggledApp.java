package com.etgames.alarmme;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(
        tableName = "toggled_apps",
        primaryKeys = {"ruleId", "appName"},
        foreignKeys = @ForeignKey(
                entity = Rule.class,
                parentColumns = "id",
                childColumns = "ruleId",
                onDelete = ForeignKey.CASCADE
        )
)
public class ToggledApp {
    public long ruleId;

    @NonNull
    public String appName;

    public ToggledApp(long ruleId, @NonNull String appName) {
        this.ruleId = ruleId;
        this.appName = appName;
    }
}
