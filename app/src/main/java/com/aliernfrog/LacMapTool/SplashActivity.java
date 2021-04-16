package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;

public class SplashActivity extends AppCompatActivity {
    SharedPreferences update;
    SharedPreferences.Editor updateEdit;
    SharedPreferences config;
    SharedPreferences.Editor configEdit;

    String external = Environment.getExternalStorageDirectory().toString(); //external storage path
    String docs; //documents folder path

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        update = getSharedPreferences("APP_UPDATE", Context.MODE_PRIVATE);
        config = getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE);
        updateEdit = update.edit();
        configEdit = config.edit();

        if (Build.VERSION.SDK_INT >= 19) {
            docs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath();
        } else {
            docs = external+"/Documents/";
        }

        setConfig();
    }

    private void setConfig() {
        try {
            String pathLacd = external+"/Android/data/com.MA.LACD/files/editor/";
            String pathLacm = external+"/Android/data/com.MA.LACM/files/editor/";
            String pathLegacy = external+"/Android/data/com.MA.LAC/files/editor/";
            updateEdit.putString("path-lac", external+"/Android/data/com.MA.LAC/files/editor/");
            updateEdit.putString("path-app", docs+"/LacMapTool/");
            if (config.getBoolean("enableLacd", false)) updateEdit.putString("path-lac", pathLacd);
            if (config.getBoolean("enableLacm", false)) updateEdit.putString("path-lac", pathLacm);
            if (config.getBoolean("enableLegacyPath", false)) updateEdit.putString("path-lac", pathLegacy);
            updateEdit.commit();
            switchActivity(MainActivity.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchActivity(Class i) {
        Intent intent = new Intent(this.getApplicationContext(), i);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            startActivity(intent);
            finish();
        }, 1500);
    }
}