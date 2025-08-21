package com.etgames.alarmme;

import androidx.room.TypeConverter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Converters {

    private static final String SEPARATOR = ";;"; // choose a separator that won't appear in your data

    @TypeConverter
    public static String fromSet(Set<String> set) {
        if (set == null || set.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (String s : set) {
            sb.append(s).append(SEPARATOR);
        }
        sb.setLength(sb.length() - SEPARATOR.length()); // remove last separator
        return sb.toString();
    }

    @TypeConverter
    public static Set<String> toSet(String value) {
        Set<String> result = new HashSet<>();
        if (value == null || value.isEmpty()) return result;
        String[] items = value.split(SEPARATOR);
        result.addAll(Arrays.asList(items));
        return result;
    }
}
