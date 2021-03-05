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
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.aliernfrog.LacMapTool.utils.BackgroundTask;
import com.aliernfrog.LacMapTool.utils.WebUtil;

import org.json.JSONObject;

public class SplashActivity extends AppCompatActivity {
    ImageView icon;
    TextView log;
    ScrollView scrollView;

    SharedPreferences update;
    SharedPreferences.Editor updateEdit;
    SharedPreferences config;
    SharedPreferences.Editor configEdit;

    private final String external = Environment.getExternalStorageDirectory().toString();
    private String docs;
    private Boolean skipUpdate;
    private String versionsURL = "https://blursedbots.glitch.me/apps/lacmaptool/update";
    private String updateURL = "https://blursedbots.glitch.me/apps/lacmaptool/versions/";

    Boolean devMode;
    String logs = "";
    Integer vers;
    Integer updatedVers = 0;
    Integer postUpdate = 0;
    Integer icon_clickCount = 0;

    String current = null;

    int VERSIONS_FILE_CODE = 1;
    int CURRENT_FILE_CODE = 2;
    int UPDATED_FILE_CODE = 3;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        icon = findViewById(R.id.splash_app_icon);
        log = findViewById(R.id.splash_log);
        scrollView = findViewById(R.id.splash_scroll);

        update = getSharedPreferences("APP_UPDATE", Context.MODE_PRIVATE);
        config = getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE);
        updateEdit = update.edit();
        configEdit = config.edit();
        skipUpdate = config.getBoolean("disableUpdates", false);
        devMode = config.getBoolean("enableDebug", false);

        if (config.getBoolean("enableTest", false)) {
            updateURL = updateURL+"test/";
            versionsURL = versionsURL+"-test";
        }

        try {
            vers = AppUtil.getVersCode(getApplicationContext());
        } catch (Exception e) {
            vers = 7;
            e.printStackTrace();
            devLog(e.toString(), true);
        }

        if (!devMode) scrollView.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= 19) {
            docs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath();
        } else {
            docs = external+"/Documents/";
            devLog("SDK version is not greater than 19", false);
        }

        setListeners();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                checkUpdates();
            }
        }, 100);
    }

    public void checkUpdates() {
        if (!skipUpdate) {
            getContentFromURL(versionsURL+".json", VERSIONS_FILE_CODE);
        } else {
            devLog("skipping updates", false);
            switchActivity(MainActivity.class);
        }
    }

    public void writeUpdate() {
        updateEdit.putBoolean("update-available", false);
        updateEdit.putString("update-changelog", "");
        updateEdit.putString("update-download", "");
        updateEdit.commit();
        try {
            JSONObject obj = new JSONObject(replaceString(current));
            updateEdit.putBoolean("blockAccess", obj.getBoolean("blockAccess"));
            updateEdit.putString("changelog", obj.getString("changelog"));
            updateEdit.putString("notes", obj.getString("notes").replace("%CHANGELOG%", obj.getString("changelog")));
            updateEdit.putString("path-lac", obj.getString("path-lac").replaceAll(">", "/"));
            updateEdit.putString("path-lacd", obj.getString("path-lacd").replaceAll(">", "/"));
            updateEdit.putString("path-legacy", obj.getString("path-legacy").replaceAll(">", "/"));
            updateEdit.putString("path-app", obj.getString("path-app").replaceAll(">", "/"));
            updateEdit.putBoolean("showAndroid11warning", obj.getBoolean("showAndroid11warning"));
            updateEdit.putBoolean("showLegacyMode", obj.getBoolean("showLegacyMode"));
            updateEdit.putInt("postUpdate", postUpdate);
            updateEdit.commit();
            if (updatedVers > vers) {
                devLog("an updated version file found", false);
                getContentFromURL(updateURL+updatedVers+".json", UPDATED_FILE_CODE);
            } else {
                switchActivity(MainActivity.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString(), true);
            offlineUpdate();
        }
    }

    public void setUpdateConfig(String string) {
        try {
            JSONObject updatedObj = new JSONObject(string);
            updateEdit.putBoolean("update-available", true);
            updateEdit.putString("update-changelog", updatedObj.getString("changelog"));
            updateEdit.putString("update-download", updatedObj.getString("download"));
            updateEdit.commit();
            switchActivity(MainActivity.class);
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString(), true);
            switchActivity(MainActivity.class);
        }
    }

    public void offlineUpdate() {
        try {
            devLog("attempting to do offline update", false);
            JSONObject obj = new JSONObject(replaceString("{\"blockAccess\": false,\"changelog\": \"No changelog\",\"notes\": \"Notes\",\"path-lac\": \"_EXTERNAL_>Android>data>com.MA.LAC>files>editor>\",\"path-lacd\": \"_EXTERNAL_>Android>data>com.MA.LACD>files>editor>\",\"path-legacy\": \"_EXTERNAL_>Android>data>com.MA.LAC>files>editor>\",\"path-app\": \"_DOCS_>LacMapTool>\",\"showAndroid11warning\": true,\"showLegacyMode\": false}"));
            updateEdit.putBoolean("update-available", false);
            updateEdit.putString("update-changelog", "");
            updateEdit.putString("update-download", "");
            updateEdit.putBoolean("blockAccess", obj.getBoolean("blockAccess"));
            if (update.getString("changelog", null) == null) updateEdit.putString("changelog", obj.getString("changelog"));
            if (update.getString("notes", null) == null) updateEdit.putString("notes", obj.getString("notes").replace("%CHANGELOG%", obj.getString("changelog")));
            if (update.getString("path-lac", null) == null) updateEdit.putString("path-lac", obj.getString("path-lac").replaceAll(">", "/"));
            if (update.getString("path-lacd", null) == null) updateEdit.putString("path-lacd", obj.getString("path-lacd").replaceAll(">", "/"));
            if (update.getString("path-legacy", null) == null) updateEdit.putString("path-legacy", obj.getString("path-legacy").replaceAll(">", "/"));
            if (update.getString("path-app", null) == null) updateEdit.putString("path-app", obj.getString("path-app").replaceAll(">", "/"));
            if (update.getBoolean("showAndroid11warning", true)) updateEdit.putBoolean("showAndroid11warning", obj.getBoolean("showAndroid11warning"));
            if (!update.getBoolean("showLegacyMode", false)) updateEdit.putBoolean("showLegacyMode", obj.getBoolean("showLegacyMode"));
            updateEdit.commit();
            switchActivity(MainActivity.class);
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString(), true);
        }
    }

    public void handleBackgroundTasks(String string, int request) {
        devLog("received: "+request, false);
        if (string != null) {
            try {
                if (request == VERSIONS_FILE_CODE) {
                    JSONObject object = new JSONObject(string);
                    postUpdate = object.getInt("postUpdate");
                    updatedVers = object.getInt("latest");
                    getContentFromURL(updateURL+vers+".json", CURRENT_FILE_CODE);
                }
                if (request == CURRENT_FILE_CODE) {
                    current = string;
                    writeUpdate();
                }
                if (request == UPDATED_FILE_CODE) {
                    setUpdateConfig(string);
                }
            } catch (Exception e) {
                e.printStackTrace();
                devLog(e.toString(), false);
                offlineUpdate();
            }
        } else {
            devLog(request+" is null!", false);
            offlineUpdate();
        }
    }

    String replaceString(String string) {
        return string.replaceAll("_EXTERNAL_", external).replaceAll("_DOCS_", docs);
    }

    public void getContentFromURL(String urlString, int request) {
        devLog("attempting to get content from URL: "+urlString, false);
        final String[] str = {null};
        new BackgroundTask(this) {
            @Override
            public void doInBackground() {
                try {
                    str[0] = WebUtil.getContentFromURL(urlString);
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            devLog(e.toString(), true);
                        }
                    });
                }
            }
            @Override
            public void onPostExecute() {
                handleBackgroundTasks(str[0], request);
            }
        }.execute();
    }

    public void switchActivity(Class i) {
        devLog("attempting to switch to class: "+i.toString(), false);
        Intent intent = new Intent(this.getApplicationContext(), i);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                startActivity(intent);
                finishActivity(0);
            }
        }, 1500);
    }

    void devLog(String toLog, Boolean error) {
        if (devMode) {
            String tag = Thread.currentThread().getStackTrace()[3].getMethodName();
            if (error) toLog = "<font color=red>"+toLog+"</font>";
            logs = logs+"<br /><font color=#00FFFF>["+tag+"]</font> "+toLog;
            log.setText(Html.fromHtml(logs));
        }
    }

    void setListeners() {
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!devMode) {
                    icon_clickCount = icon_clickCount+1;
                    if (icon_clickCount >= 5) {
                        configEdit.putBoolean("enableDebug", true);
                        configEdit.commit();
                        finish();
                    }
                }
            }
        });
    }
}