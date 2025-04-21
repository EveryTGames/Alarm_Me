package com.etgames.alarmme;

import static com.etgames.alarmme.MainActivity.loadedApps;
import static com.etgames.alarmme.MainActivity.toggledApps;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class toggleAppsActivity extends AppCompatActivity {

    Runnable searchRunnable;
    Handler handler = new Handler(Looper.getMainLooper());
    int delay = 1000;
    appListAdapter adapter;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toggle_apps_layout);

        Log.d("infoo", "the new activity started");


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new appListAdapter(this);

        adapter.submitList(loadedApps);


        recyclerView.setAdapter(adapter);


        EditText searchEditText = findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        if (searchRunnable != null) {
                            handler.removeCallbacks(searchRunnable);
                        }

                        searchRunnable = () -> {
                            String searchQuery = charSequence.toString().trim();
                            filter(searchQuery);

                        };

                        handler.postDelayed(searchRunnable, delay);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                }
        );
        ImageButton deleteSearchBtn = findViewById(R.id.delBtn);
        deleteSearchBtn.setOnClickListener(v ->
                searchEditText.setText("")
        );


    }

    ExecutorService background = Executors.newSingleThreadExecutor();


    void filter(@NonNull String searchQuery) {
        background.execute(

                () ->
                {
                    List<app_info> filteredList;


                    if (!searchQuery.isEmpty()) {
                        filteredList = new ArrayList<>();
                        for (app_info item : loadedApps) {
                            if (item.appName.toLowerCase().contains(searchQuery.toLowerCase()))
                                filteredList.add(item);
                        }
                    } else {
                        handler.post(() -> {
                            adapter.submitList(loadedApps);
                        });
                        return;
                    }


                    handler.post(() -> {
                        adapter.submitList(filteredList);
                    });
                }
        );


    }


}

