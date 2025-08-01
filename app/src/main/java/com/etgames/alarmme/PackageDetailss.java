package com.etgames.alarmme;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PackageDetailss {
    public String[] preferedSenderName;
    public boolean isAnd;
    public String[] preferedContentCommands;
    public boolean deepSleepMode;



    public PackageDetailss(Context context)
    {
        prefs = context.getSharedPreferences("ALARM_APP", MODE_PRIVATE);

    }
    public static String[] getRulesIDs(String PackageName) {
        String value = prefs.getString(PackageName, null);

        if (value != null) {
            return value.split(",");
            // use ids
        } else {
            return null;
        }
    }
    static SharedPreferences  prefs;
    public PackageDetailss retrieve(String ruleID) {

        //the encoded data is like this
        //                                   "or"  or "and" to determine if the user wants the sender name and one of the content messages to match in order to trigger the alarm, or just want any one of them
        //value : "<preferered sender name 1>:#encode_split_sub#:<preferered sender name 2 ... 3 and so on>:#encode_split_main#:<"or"  or "and" >:#encode_split_main#:<prefered content message 1>:#encode_split_sub#:<prefered content message 2 ... and so on>:#encode_split_main#:<deepSleepMode on or off (default off)>"


        String encodedData = prefs.getString(ruleID, null);

        if (encodedData != null) {

            String[] decodedDataMain = encodedData.split(MainActivity.ENCODE_SPLIT_MAIN);
            preferedSenderName = decodedDataMain[0].split(MainActivity.ENCODE_SPLIT_SUBs);
            isAnd = decodedDataMain[1].equals("and");
            if (decodedDataMain.length == 4) {

                preferedContentCommands = decodedDataMain[2].split(MainActivity.ENCODE_SPLIT_SUBs);
            } else {
                preferedContentCommands = new String[]{""};
            }
            if (decodedDataMain.length == 4) {

                deepSleepMode = decodedDataMain[3].equals("on");
            } else {
                Log.d("infoo", "from old preference");
                deepSleepMode = false;
            }
            return this;
        } else {

            Log.e("infoo", "be carefule if u encounter an error here");

            return null;
        }

    }

}
