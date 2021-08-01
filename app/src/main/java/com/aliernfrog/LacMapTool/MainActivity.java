package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliernfrog.LacMapTool.utils.AppUtil;

import java.io.File;
import java.io.IOException;

@SuppressLint({"CommitPrefEdits", "ClickableViewAccessibility"})
public class MainActivity extends AppCompatActivity {
    LinearLayout missingPerms;
    LinearLayout lacLinear;
    Button redirectMaps;
    Button redirectWallpaper;
    Button redirectGallery;
    LinearLayout appLinear;
    Button checkUpdates;
    Button redirectOptions;
    LinearLayout updateLinear;
    TextView updateLinearTitle;
    TextView updateLog;
    TextView log;

    String dataPath;
    String lacPath;
    String backupPath;
    String aBackupPath;
    Boolean hasPerms;
    Integer version;

    String logs = "";
    Boolean devMode;

    SharedPreferences update;
    SharedPreferences config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        update = getSharedPreferences("APP_UPDATE", Context.MODE_PRIVATE);
        config = getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE);
        devMode = config.getBoolean("enableDebug", false);

        devMode = config.getBoolean("enableDebug", false);
        lacPath = update.getString("path-lac", null);
        dataPath = update.getString("path-app", null);
        backupPath = dataPath+"backups/";
        aBackupPath = dataPath+"auto-backups/";
        version = update.getInt("versionCode", 0);

        missingPerms = findViewById(R.id.main_missingPerms);
        lacLinear = findViewById(R.id.main_optionsLac);
        redirectMaps = findViewById(R.id.main_maps);
        redirectWallpaper = findViewById(R.id.main_wallpapers);
        redirectGallery = findViewById(R.id.main_screenshots);
        appLinear = findViewById(R.id.main_optionsApp);
        checkUpdates = findViewById(R.id.main_checkUpdates);
        redirectOptions = findViewById(R.id.main_options);
        updateLinear = findViewById(R.id.main_update);
        updateLinearTitle = findViewById(R.id.main_update_title);
        updateLog = findViewById(R.id.main_update_description);
        log = findViewById(R.id.main_log);

        if (devMode) log.setVisibility(View.VISIBLE);
        checkUpdates();
        checkPerms();
        createFiles();
        setListeners();
    }

    public void getUpdates() {
        devLog("attempting to get updates from website");
        try {
            if (AppUtil.getUpdates(getApplicationContext())) {
                checkUpdates();
            }
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString());
        }
    }

    public void checkUpdates() {
        devLog("checking for updates");
        int latest = update.getInt("updateLatest", 0);
        String download = update.getString("updateDownload", null);
        String changelog = update.getString("updateChangelog", null);
        String changelogVersion = update.getString("updateChangelogVersion", null);
        String notes = update.getString("notes", null);
        boolean hasUpdate = latest > version;
        boolean linearVisible = false;
        String full = "";
        if (hasUpdate) {
            linearVisible = true;
            full = changelog+"<br /><br /><b>"+getString(R.string.optionsChangelogChangelog)+":</b> "+changelogVersion;
            updateLinearTitle.setVisibility(View.VISIBLE);
            updateLinear.setBackground(ContextCompat.getDrawable(getApplicationContext() ,R.drawable.linear_blue));
            updateLinear.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    redirectURL(download);
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            });
        } else {
            if (notes != null && !notes.equals("")) {
                linearVisible = true;
                full = notes;
            }
            updateLinear.setOnTouchListener((v, event) -> {
                AppUtil.handleOnPressEvent(v, event);
                return true;
            });
        }
        updateLog.setText(Html.fromHtml(full));
        if (linearVisible) updateLinear.setVisibility(View.VISIBLE);
    }

    public void checkPerms() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                hasPerms = false;
                missingPerms.setVisibility(View.VISIBLE);
                devLog("permission denied, attempting to request permission");
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
            } else {
                hasPerms = true;
                missingPerms.setVisibility(View.GONE);
                devLog("permissions granted");
            }
        } else {
            hasPerms = true;
            missingPerms.setVisibility(View.GONE);
            devLog("old SDK version detected");
        }
    }

    public void createFiles() {
        File dataFolder = new File(dataPath);
        File backupFolder = new File(backupPath);
        File aBackupFolder = new File(aBackupPath);
        File lacFolder = new File(lacPath);
        File wallpaperFolder = new File(lacPath.replace("editor", "wallpaper"));
        File nomedia = new File(dataPath+".nomedia");
        if (!dataFolder.exists()) mkdirs(dataFolder);
        if (!backupFolder.exists()) mkdirs(backupFolder);
        if (!aBackupFolder.exists()) mkdirs(aBackupFolder);
        if (!lacFolder.exists()) mkdirs(lacFolder);
        if (!wallpaperFolder.exists()) mkdirs(wallpaperFolder);
        if (!nomedia.exists()) {
            try {
                nomedia.createNewFile();
            } catch (IOException e) {
                devLog(e.toString());
            }
        }
    }

    public void switchActivity(Class i, Boolean allowWithoutPerms) {
        if (!allowWithoutPerms && !hasPerms) {
            //if no enough permissions, check them again
            devLog("no required permissions, checking again");
            checkPerms();
        } else {
            Intent intent = new Intent(this.getApplicationContext(), i);
            devLog("attempting to redirect to "+i.toString());
            if (i == OptionsActivity.class) {
                startActivityForResult(intent, 5);
            } else {
                startActivity(intent);
            }
        }
    }

    public void mkdirs(File mk) {
        boolean state = mk.mkdirs();
        devLog(mk.getPath()+" //"+state);
    }

    public void redirectURL(String url) {
        devLog("attempting to redirect to:"+url);
        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        startActivity(viewIntent);
    }

    void devLog(String toLog) {
        if (devMode) {
            String tag = Thread.currentThread().getStackTrace()[3].getMethodName();
            if (toLog.contains("Exception")) toLog = "<font color=red>"+toLog+"</font>";
            logs = logs+"<br /><font color=#00FFFF>["+tag+"]</font> "+toLog;
            log.setText(Html.fromHtml(logs));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 5) {
            if (resultCode == 1) {
                switchActivity(SplashActivity.class, true);
                finish();
            }
        }
    }

    public void setListeners() {
        missingPerms.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                checkPerms();
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        lacLinear.setOnTouchListener((v, event) -> {
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        redirectMaps.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                switchActivity(MapsActivity.class, false);
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        redirectWallpaper.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                switchActivity(WallpaperActivity.class, false);
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        redirectGallery.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                switchActivity(GalleryActivity.class, false);
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        appLinear.setOnTouchListener((v, event) -> {
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        checkUpdates.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                getUpdates();
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        redirectOptions.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                switchActivity(OptionsActivity.class, true);
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
    }
}