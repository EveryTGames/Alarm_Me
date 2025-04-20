package com.etgames.alarmme;

import static com.etgames.alarmme.MainActivity.toggledApps;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class toggleAppsActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toggle_apps_layout);

        Log.d("infoo", "the new activity started");

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> installedApps = packageManager.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN, null)
                        .addCategory(Intent.CATEGORY_LAUNCHER),
                0);

        Log.d("infoo", "the installed apps list should be retrived with " + installedApps.size() + " of elemetns");

        Toast.makeText(getApplicationContext(), "loading Apps...", Toast.LENGTH_SHORT).show();


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        appListAdapter adapter = new appListAdapter(this);

        List<app_info> appInfoList = new ArrayList<>();

        for (ResolveInfo resolvedInfo : installedApps) {

            boolean isToggled = toggledApps.contains(resolvedInfo.activityInfo.packageName);
            appInfoList.add(new app_info(resolvedInfo.loadLabel(packageManager).toString(), resolvedInfo.activityInfo.packageName, resolvedInfo.loadIcon(packageManager), isToggled));
        }


        adapter.submitList(appInfoList);

        recyclerView.setAdapter(adapter);
    }





}

