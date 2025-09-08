package com.etgames.alarmme;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.InvalidationTracker;

import com.etgames.alarmme.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469;
    public static String ENCODE_SPLIT_SUBs = ":#94710341#:";
    public static String ENCODE_SPLIT_MAIN = ":#41037124#:";

    int REQUEST_CODE = 59;
    private static final int JOB_ID = 1;
    public static final String CHANNEL_ID = "myAlarm";

    private ActivityMainBinding binding;
    public static boolean isLoaded = false;


    public static Set<String> toggledAppsForCurrentRule;
    public static SharedPreferences prefs;




    public static Set<String> getToggledAppsForRule(String ruleId) {
        return new HashSet<>(prefs.getStringSet("toggled_apps_" + ruleId, new HashSet<>()));
    }


    public static List<app_info> loadedApps;
    private AppBarConfiguration mAppBarConfiguration;
    public static int lastID;

  public static   RuleDataBase db;

    FabViewModel fabViewModel;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        prefs = getSharedPreferences("ALARM_APP", MODE_PRIVATE);

        db =  RuleDataBase.getDatabase(getApplicationContext());
        //prefs.edit().putBoolean("adsOn",true).apply();
        if (prefs.getBoolean("adsOn", false)) {

            AdManager.init(this); // Start Unity Ads
        }


        int permissionState = ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS);
        // If the permission is not granted, request it.
        if (permissionState == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
        }


        setSupportActionBar(binding.appBarMain.toolbar);


        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_alarms, R.id.nav_rules, R.id.nav_settings)
                .setOpenableLayout(drawer)
                .build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);




        prefs.edit().putBoolean("isOn", true).apply();

        new Thread(() -> {
            AlarmDataBase db = AlarmDataBase.getDatabase(MainActivity.this.getApplicationContext());
            List<Long> enabledAlarms = db.alarmDao().getAllEnabledAlarmIds();
            Set<String> idSet = new HashSet<>();
            for (Long id : enabledAlarms) {
                idSet.add(String.valueOf(id));
            }

        }).start();

        Log.d("infoo", prefs.getString("succesfullyRegisteredAlarmsAfterBoot", "not there :)"));

        // i copied it again bc the returned one was a live set
        //which means any change in it will be changed in the prefs, which will make weird behaviours
        // for my case it will not update after the first load


        Log.d("infoo", " " + isNotificationListenerEnabled(this));


        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);


        lastID = Integer.parseInt(prefs.getString("lastID", "0"));


        fabViewModel = new ViewModelProvider(this).get(FabViewModel.class);


        fabViewModel.quiteFabClicked.observe(this, unused -> {


            StopService();
            // Toast.makeText(this, "FAB clicked! in main activity", Toast.LENGTH_SHORT).show();
        });

        fabViewModel.getShowFab().observe(this, visible -> {
            if (visible != null) {
                binding.appBarMain.fab.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        });
        fabViewModel.getShowQuiteFab().observe(this, visible -> {
            if (visible != null) {
                binding.appBarMain.quiteFab.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        });

        // FAB click listener (optional - can trigger ViewModel method if Fragment uses FAB)
        binding.appBarMain.fab.setOnClickListener(view -> {
            fabViewModel.triggerFabClick();
        });
        binding.appBarMain.quiteFab.setOnClickListener(view -> {
            fabViewModel.triggerQuiteFabClick();
        });


        if (!isLoaded) {

            loadInstalledAppsAsync(() -> {
                isLoaded = true;
                Toast.makeText(this, "App list loaded", Toast.LENGTH_SHORT).show();

            });
        }

    }

    public interface AppLoadCallback {
        void onLoaded();
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Navigate to SettingsFragment
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_settings); // make sure this ID matches your nav_graph
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private final Handler handler = new Handler(Looper.getMainLooper());

    public void triggerTheAlarm(boolean deepSleepMode, int hour, int minute) {


        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("deepSleepMode", deepSleepMode);
        Log.d("infoo", "deep sleep mode in appnotification listenerSercice is " + deepSleepMode);
        intent.setAction("com.etgames.trigerAlarm");
        // Use FLAG_UPDATE_CURRENT to ensure the new extras are respected
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_background).setContentTitle("ALARM Set").setContentText("the Alarm set on " + hour + ":" + minute).setDefaults(Notification.DEFAULT_SOUND).setAutoCancel(true);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(652, builder.build());


        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);


        Log.d("infoo", "command revcived, alarm triggered");


    }

    private static final String LOGGER_TAG = "infoo";


    public void loadInstalledAppsAsync(AppLoadCallback callback) {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Toast.makeText(getApplicationContext(), "loading App List...", Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
            loadInstalledApps();

            handler.post(callback::onLoaded);
        });
    }


    private void loadInstalledApps() {


        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> installedApps = packageManager.queryIntentActivities(
                    new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER),
                    0
            );

            List<app_info> tempList = new ArrayList<>();

            for (ResolveInfo resolvedInfo : installedApps) {
                String packageName = resolvedInfo.activityInfo.packageName;

                //  Call the database to check toggle status (in background)
                boolean isToggled = MainActivity.db.ruleDao().isAppToggled(packageName);

                app_info app = new app_info(
                        resolvedInfo.loadLabel(packageManager).toString(),
                        packageName,
                        resolvedInfo.loadIcon(packageManager),
                        isToggled
                );

                tempList.add(app);
            }

            //  Update `loadedApps` and post to UI thread if needed
            loadedApps = tempList;

            runOnUiThread(() -> {
                // e.g., notify adapter or Toast
                Toast.makeText(this, "Apps loaded with toggle states", Toast.LENGTH_SHORT).show();
            });
        });

    }


    //Helperr method to show a dialog window
    private void showMessageForFloatingPermission(String message) {
        new android.app.AlertDialog.Builder(MainActivity.this).setMessage(message).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                checkFloatingPermission();

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //User opted not to use this feature
                //finish();

            }
        }).create().show();
    }


    //Helperr method for checking over lay floating permission
    public void checkFloatingPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityFloatingPermission.launch(intent);//this will open device settings for over lay permission window

        }
    }


    //Initialize ActivityResultLauncher. Note here that no need custom request code

    ActivityResultLauncher<Intent> startActivityFloatingPermission = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                //Permission granted
            } else {
                //If there is no permission allowed yet, still a dialog window will open unless a user opted not to use the feature.
                if (!Settings.canDrawOverlays(MainActivity.this)) {
                    // You don't have permission yet, show a dialog reasoning
                    showMessageForFloatingPermission("To use this feature requires over lay permission");

                }
            }
        }
    });


    private boolean isNotificationListenerEnabled(Context context) {
        ComponentName cn = new ComponentName(context, AppNotificationListenerService.class);
        String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(cn.flattenToString());
    }


    public void PermissionService(View v) {

        if (!isNotificationListenerEnabled(this)) {
            // Request permission
            AppNotificationListenerService.requestNotificationListenerAccess(this);
        }

        int permissionState = ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS);
        // If the permission is not granted, request it.
        if (permissionState == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
        }


        if ("xiaomi".equalsIgnoreCase(Build.MANUFACTURER) && !Settings.canDrawOverlays(this)) {
            try {
                Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                intent.putExtra("extra_pkgname", getPackageName());
                startActivity(intent);
            } catch (Exception e) {
                // Fallback if the above intent fails
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(this, "Unable to open permissions settings", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            }
        }


    }

    public void StopService() {
        // Remove the permanent notification
        prefs.edit().putBoolean("isOn", false).apply();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(55);
        }
        // Close the app completely
        finishAffinity(); // Close all activities
        System.exit(0);   // Force the app process to stop
    }

    @Override
    protected void onRestart() {
        Log.d("infoo", "restart have been called from main activity, ?");

        super.onRestart();
    }


    public boolean stop() {


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_background).setContentTitle("the app is closed").setContentText("the service stopped, will no longer be able to listen to messages").setPriority(NotificationCompat.PRIORITY_DEFAULT).setOngoing(false);
        Log.d("infoo", "ummmmm");

        // delete the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(55, builder.build());

        Log.d("infoo", "stop function have been called from main activity, service stopped?");
        return true;

    }

    PackageManager packageManager;
    ComponentName componentName;

    @Override
    protected void onStart() {
        Log.d("infoo", "on start have been called from main activity, service stopped?");
        packageManager = getPackageManager();
        componentName = new ComponentName(this, AppNotificationListenerService.class);

        // Disable the component:
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
// Enable the component:
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);


        super.onStart();

    }

    @Override
    public void onDestroy() {
        Log.d("infoo", "destroy have been called from main activity, service stopped?");


        // Log.d("infoo", "the sstop function returned with  " + stop());

        Log.d("infoo", "hmmmmmmmm");


        super.onDestroy();

    }


}