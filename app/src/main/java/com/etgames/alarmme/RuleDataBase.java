package com.etgames.alarmme;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Rule.class, ToggledApp.class, SelectedRule.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class RuleDataBase extends RoomDatabase {

    private static volatile RuleDataBase INSTANCE;

    public abstract RuleDao ruleDao();
    public abstract SelectedRuleDao selectedRuleDao();

    public static RuleDataBase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RuleDataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder (context.getApplicationContext(),
                                    RuleDataBase.class, "rule_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
