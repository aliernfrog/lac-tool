package com.aliernfrog.lactool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.TextView;

import com.aliernfrog.LacMapTool.R;
import com.aliernfrog.lactool.utils.AppUtil;

@SuppressLint({"CommitPrefEdits", "CustomSplashScreen"})
public class SplashActivity extends AppCompatActivity {
    TextView debugText;

    SharedPreferences prefsUpdate;
    SharedPreferences prefsConfig;
    SharedPreferences.Editor prefsEditUpdate;
    SharedPreferences.Editor prefsEditConfig;

    Integer switchDelay = 1000;

    String pathExternal = Environment.getExternalStorageDirectory().toString();
    String pathDocs = pathExternal+"/Documents";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        debugText = findViewById(R.id.splash_debug);

        prefsUpdate = getSharedPreferences("APP_UPDATE", MODE_PRIVATE);
        prefsConfig = getSharedPreferences("APP_CONFIG", MODE_PRIVATE);
        prefsEditUpdate = prefsUpdate.edit();
        prefsEditConfig = prefsConfig.edit();

        if (Build.VERSION.SDK_INT >= 19) pathDocs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath();

        if (prefsConfig.getBoolean("enableDebug", false)) debugText.setVisibility(View.VISIBLE);

        devLog("SplashActivity started");
        devLog("Android SDK version: "+Build.VERSION.SDK_INT);
        devLog("External path: "+pathExternal);
        devLog("Documents path: "+pathDocs);

        getVersion();
        setTheme();
        checkUpdates();
    }

    public void getVersion() {
        devLog("attempting to get version");
        try {
            String versName = AppUtil.getVersName(getApplicationContext());
            Integer versCode = AppUtil.getVersCode(getApplicationContext());
            devLog("version name: "+versName);
            devLog("version code: "+versCode);
            prefsEditUpdate.putString("versionName", versName);
            prefsEditUpdate.putInt("versionCode", versCode);
            prefsEditUpdate.commit();
            devLog("saved version name & code");
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString());
        }
    }

    public void checkUpdates() {
        boolean shouldCheck = prefsConfig.getBoolean("autoCheckUpdates", true);
        devLog("should check for updates: "+shouldCheck);
        if (shouldCheck) {
            try {
                if (AppUtil.getUpdates(getApplicationContext())) devLog("saved update config");
            } catch (Exception e) {
                e.printStackTrace();
                devLog(e.toString());
                devLog("something went wrong, skipping updates");
            }
        } else {
            devLog("skipping updates");
        }
        setConfig();
    }

    public void setTheme() {
        int theme = prefsConfig.getInt("appTheme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        devLog("attempting to set theme: "+theme);
        AppCompatDelegate.setDefaultNightMode(theme);
    }

    public void setConfig() {
        devLog("attempting to set config");
        String pathLac = pathExternal+"/Android/data/com.MA.LAC/files";
        String pathLacd = pathExternal+"/Android/data/com.MA.LACD/files";
        String pathLacm = pathExternal+"/Android/data/com.MA.LACM/files";
        String pathLacmb = pathExternal+"/Android/data/com.MA.LACMB/files";
        String pathApp = pathDocs+"/LacMapTool/";
        String pathTemp = pathApp+"temp";
        String lacId = prefsConfig.getString("lacId", "lac");
        if (lacId.equals("lacd")) pathLac = pathLacd;
        if (lacId.equals("lacm")) pathLac = pathLacm;
        if (lacId.equals("lacmb")) pathLac = pathLacmb;
        prefsEditUpdate.putString("path-maps", pathLac+"/editor");
        prefsEditUpdate.putString("path-wallpapers", pathLac+"/wallpaper");
        prefsEditUpdate.putString("path-screenshots", pathLac+"/screenshots");
        prefsEditUpdate.putString("path-lac", pathLac);
        prefsEditUpdate.putString("path-app", pathApp);
        prefsEditUpdate.putString("path-temp", pathTemp);
        prefsEditUpdate.putString("path-temp-maps", pathTemp+"/editor");
        prefsEditUpdate.putString("path-temp-wallpapers", pathTemp+"/wallpaper");
        prefsEditUpdate.putString("path-temp-screenshots", pathTemp+"/screenshots");
        prefsEditUpdate.commit();
        devLog("set config");
        clearTempData(pathTemp);
    }

    public void clearTempData(String tempPath) {
        devLog("attempting to clear temp data");
        AppUtil.clearTempData(tempPath);
        switchActivity();
    }

    public void switchActivity() {
        devLog("attempting to switch in "+switchDelay);
        Intent intent = new Intent(this.getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    void devLog(String toLog) {
        AppUtil.devLog(toLog, debugText);
    }
}