package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliernfrog.LacMapTool.utils.AppUtil;

import java.io.File;
import java.io.IOException;

@SuppressLint("ClickableViewAccessibility")
public class MainActivity extends AppCompatActivity {
    LinearLayout missingPerms;
    LinearLayout optionsLinear;
    LinearLayout redirectMaps;
    LinearLayout redirectWallpaper;
    LinearLayout redirectGallery;
    LinearLayout redirectOptions;
    TextView log;

    String logs = "";
    Boolean devMode;
    Boolean hasPerms;
    String dataPath;
    String lacPath;
    String backupPath;
    String aBackupPath;

    SharedPreferences update;
    SharedPreferences config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
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

        missingPerms = findViewById(R.id.main_missingPerms);
        optionsLinear = findViewById(R.id.main_options);
        redirectMaps = findViewById(R.id.main_maps_linear);
        redirectWallpaper = findViewById(R.id.main_wallpaper_linear);
        redirectGallery = findViewById(R.id.main_gallery_linear);
        redirectOptions = findViewById(R.id.main_settings_linear);
        log = findViewById(R.id.main_log);

        if (!devMode) log.setVisibility(View.GONE);
        checkPerms();
        createFiles();
        setListeners();
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
        File wallpaperFolder = new File(lacPath.replace("editor/", "wallpaper/"));
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

        optionsLinear.setOnTouchListener((v, event) -> {
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

        redirectOptions.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                switchActivity(OptionsActivity.class, true);
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
    }
}