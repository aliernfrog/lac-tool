package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;

import com.aliernfrog.LacMapTool.utils.AppUtil;

import java.util.Locale;

@SuppressLint("CommitPrefEdits")
public class SplashActivity extends AppCompatActivity {
    SharedPreferences update;
    SharedPreferences.Editor updateEdit;
    SharedPreferences config;
    SharedPreferences.Editor configEdit;

    String versionName = "-";
    Integer versionCode = 0;
    String external = Environment.getExternalStorageDirectory().toString(); //external storage path
    String docs; //documents folder path

    Boolean forceEnglish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        update = getSharedPreferences("APP_UPDATE", Context.MODE_PRIVATE);
        config = getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE);
        updateEdit = update.edit();
        configEdit = config.edit();

        forceEnglish = config.getBoolean("forceEnglish", false);

        docs = external+"/Documents";
        if (Build.VERSION.SDK_INT >= 19) docs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath();

        getVersion();
        setLocale();
        setConfig();
    }

    void setConfig() {
        try {
            String pathLacd = external+"/Android/data/com.MA.LACD/files/editor";
            String pathLacm = external+"/Android/data/com.MA.LACM/files/editor";
            String pathLacmb = external+"/Android/data/com.MA.LACMB/files/editor";
            String pathLegacy = external+"/Android/data/com.MA.LAC/files/editor";
            updateEdit.putString("versionName", versionName);
            updateEdit.putInt("versionCode", versionCode);
            updateEdit.putString("path-lac", external+"/Android/data/com.MA.LAC/files/editor");
            updateEdit.putString("path-app", docs+"/LacMapTool/");
            if (config.getBoolean("enableLacd", false)) updateEdit.putString("path-lac", pathLacd);
            if (config.getBoolean("enableLacm", false)) updateEdit.putString("path-lac", pathLacm);
            if (config.getBoolean("enableLacmb", false)) updateEdit.putString("path-lac", pathLacmb);
            if (config.getBoolean("enableLegacyPath", false)) updateEdit.putString("path-lac", pathLegacy);
            updateEdit.commit();
            switchActivity(MainActivity.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void getVersion() {
        try {
            versionName = AppUtil.getVersName(getApplicationContext());
            versionCode = AppUtil.getVersCode(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setLocale() {
        String lang = Locale.getDefault().getLanguage();
        if (forceEnglish) lang = "en";
        setLocale(lang);
    }

    void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        Configuration configuration = res.getConfiguration();
        configuration.locale = locale;
        res.updateConfiguration(configuration, metrics);
    }

    void switchActivity(Class i) {
        Intent intent = new Intent(this.getApplicationContext(), i);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            startActivity(intent);
            finish();
        }, 1000);
    }
}