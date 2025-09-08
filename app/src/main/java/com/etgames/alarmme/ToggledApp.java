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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ToggledApp that = (ToggledApp) o;

        return ruleId == that.ruleId &&
                appName != null && appName.equals(that.appName);
    }

    @Override
    public int hashCode() {
        // Still safe, even if you're not using it — ensures correctness
        int result = Long.hashCode(ruleId);
        result = 31 * result + (appName != null ? appName.hashCode() : 0);
        return result;
    }
}
