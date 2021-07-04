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
import com.aliernfrog.LacMapTool.utils.WebUtil;

import org.json.JSONObject;

import java.util.Locale;

@SuppressLint("CommitPrefEdits")
public class SplashActivity extends AppCompatActivity {
    TextView debugText;

    SharedPreferences prefsUpdate;
    SharedPreferences prefsConfig;
    SharedPreferences.Editor prefsEditUpdate;
    SharedPreferences.Editor prefsEditConfig;

    String rawLogs = "";

    String updateUrl = "https://aliernfrog.glitch.me/lacmaptool/update.json";
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

        devLog("LAC TOOL STARTED");
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
        boolean shouldCheck = prefsConfig.getBoolean("checkUpdates", false);
        devLog("should check for updates: "+shouldCheck);
        if (shouldCheck) {
            try {
                String rawContent = WebUtil.getContentFromURL(updateUrl);
                JSONObject updateObj = new JSONObject(rawContent);
                devLog(updateObj.toString());
                prefsEditUpdate.putInt("updateLatest", updateObj.getInt("latest"));
                prefsEditUpdate.putString("updateDownload", updateObj.getString("download"));
                prefsEditUpdate.putString("updateChangelog", updateObj.getString("changelog"));
                prefsEditUpdate.commit();
                devLog("saved update config");
                setConfig();
            } catch (Exception e) {
                e.printStackTrace();
                devLog(e.toString());
                devLog("something went wrong, skipping updates");
                setConfig();
            }
        } else {
            devLog("skipping updates");
            setConfig();
        }
    }

    public void setConfig() {
        devLog("attempting to set config");
        String pathLacd = pathExternal +"/Android/data/com.MA.LACD/files/editor";
        String pathLacm = pathExternal +"/Android/data/com.MA.LACM/files/editor";
        String pathLacmb = pathExternal +"/Android/data/com.MA.LACMB/files/editor";
        String pathLegacy = pathExternal +"/Android/data/com.MA.LAC/files/editor";
        prefsEditUpdate.putString("path-lac", pathExternal +"/Android/data/com.MA.LAC/files/editor");
        prefsEditUpdate.putString("path-app", pathDocs+"/LacMapTool/");
        if (prefsConfig.getBoolean("enableLacd", false)) prefsEditUpdate.putString("path-lac", pathLacd);
        if (prefsConfig.getBoolean("enableLacm", false)) prefsEditUpdate.putString("path-lac", pathLacm);
        if (prefsConfig.getBoolean("enableLacmb", false)) prefsEditUpdate.putString("path-lac", pathLacmb);
        if (prefsConfig.getBoolean("enableLegacyPath", false)) prefsEditUpdate.putString("path-lac", pathLegacy);
        prefsEditUpdate.commit();
        devLog("set config");
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