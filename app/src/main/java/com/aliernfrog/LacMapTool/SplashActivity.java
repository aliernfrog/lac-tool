package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.aliernfrog.LacMapTool.utils.AppUtil;

import java.util.Locale;

@SuppressLint("CommitPrefEdits")
public class SplashActivity extends AppCompatActivity {
    TextView debugText;

    SharedPreferences prefsUpdate;
    SharedPreferences prefsConfig;
    SharedPreferences.Editor prefsEditUpdate;
    SharedPreferences.Editor prefsEditConfig;

    String rawLogs = "";

    Integer switchDelay = 1000;

    String pathExternal = Environment.getExternalStorageDirectory().toString();
    String pathDocs = pathExternal +"/Documents";

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

        boolean enableDebugUI = prefsConfig.getBoolean("enableDebug", false);
        if (enableDebugUI) debugText.setVisibility(View.VISIBLE);

        devLog("SplashActivity started");
        devLog("Android SDK version: "+Build.VERSION.SDK_INT);
        devLog("External path: "+ pathExternal);
        devLog("Documents path: "+pathDocs);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            getVersion();
            setLocale();
            checkUpdates();
        }, 500);
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

    public void setLocale() {
        devLog("attempting to set locale");
        String lang = Locale.getDefault().getLanguage();
        boolean forceEnglish = prefsConfig.getBoolean("forceEnglish", false);
        if (forceEnglish) lang = "en";
        Locale locale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        Configuration configuration = res.getConfiguration();
        configuration.locale = locale;
        res.updateConfiguration(configuration, metrics);
        devLog("set locale to: "+lang);
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

    public void setConfig() {
        devLog("attempting to set config");
        String pathLac = pathExternal+"/Android/data/com.MA.LAC/files";
        String pathLacd = pathExternal+"/Android/data/com.MA.LACD/files/editor";
        String pathLacm = pathExternal+"/Android/data/com.MA.LACM/files/editor";
        String pathLacmb = pathExternal+"/Android/data/com.MA.LACMB/files/editor";
        String pathApp = pathDocs+"/LacMapTool/";
        String pathTemp = pathApp+"temp";
        if (prefsConfig.getBoolean("enableLacd", false)) pathLac = pathLacd;
        if (prefsConfig.getBoolean("enableLacm", false)) pathLac = pathLacm;
        if (prefsConfig.getBoolean("enableLacmb", false)) pathLac = pathLacmb;
        prefsEditUpdate.putString("path-maps", pathLac+"/editor");
        prefsEditUpdate.putString("path-lac", pathLac+"/editor");
        prefsEditUpdate.putString("path-app", pathApp);
        prefsEditUpdate.putString("path-temp", pathTemp);
        prefsEditUpdate.putString("path-temp-maps", pathTemp+"/editor");
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
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            startActivity(intent);
            finish();
        }, switchDelay);
    }

    void devLog(String toLog) {
        if (debugText.getVisibility() == View.VISIBLE) {
            String tag = Thread.currentThread().getStackTrace()[3].getMethodName();
            if (toLog.contains("Exception")) toLog = "<font color=red>"+toLog+"</font>";
            rawLogs = rawLogs+"<br /><font color=#00FFFF>["+tag+"]</font> "+toLog;
            debugText.setText(Html.fromHtml(rawLogs));
        }
    }
}