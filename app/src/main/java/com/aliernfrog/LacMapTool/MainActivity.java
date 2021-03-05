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
import android.os.Process;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.aliernfrog.LacMapTool.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SuppressLint("ClickableViewAccessibility")
public class MainActivity extends AppCompatActivity {
    Button android11warning;
    Button missingPerms;
    LinearLayout optionsLinear;
    LinearLayout redirectMaps;
    LinearLayout redirectWallpaper;
    LinearLayout appOptionsLinear;
    LinearLayout redirectPosts;
    LinearLayout redirectOptions;
    LinearLayout updateLinear;
    TextView updateText;
    TextView updateNotes;
    Button updateButton;
    TextView log;
    LinearLayout warnLinear;

    String logs = "";
    Boolean devMode;
    Boolean hasPerms;
    String dataPath;
    String lacPath;
    String backupPath;
    String aBackupPath;

    Uri lacTreeUri;
    Uri lacUri;
    int takeFlags;

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

        android11warning = findViewById(R.id.main_android11warning);
        missingPerms = findViewById(R.id.main_missingPerms);
        optionsLinear = findViewById(R.id.main_options);
        redirectMaps = findViewById(R.id.main_maps_linear);
        redirectWallpaper = findViewById(R.id.main_wallpaper_linear);
        appOptionsLinear = findViewById(R.id.main_appOptions);
        redirectPosts = findViewById(R.id.main_posts_linear);
        redirectOptions = findViewById(R.id.main_settings_linear);
        updateButton = findViewById(R.id.main_updateApp);
        updateNotes = findViewById(R.id.main_notes);
        log = findViewById(R.id.main_log);
        updateText = findViewById(R.id.main_updateText);
        updateLinear = findViewById(R.id.main_updates);
        warnLinear = findViewById(R.id.main_warnings);

        if (Build.VERSION.SDK_INT >= 30) {
            devLog("android 11 detected", false);
            lacTreeUri = DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", "primary:Android/data/com.MA.LAC/files");
            lacUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Android/data/com.MA.LAC/files");
            takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;
            if (getApplicationContext().checkUriPermission(lacTreeUri, Process.myPid(), Process.myUid(), Intent.FLAG_GRANT_READ_URI_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                devLog("no permissions to lac data, attempting to request", false);
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                        .putExtra(DocumentsContract.EXTRA_INITIAL_URI, lacUri)
                        .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        .putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                        .addFlags(takeFlags);
                startActivityForResult(intent, 4);
            }
        }

        if (!devMode) log.setVisibility(View.GONE);
        setListeners();
        checkPerms();
        checkPostUpdate();
        createFiles();
        autoBackup();
        getLog();
    }

    public void checkPostUpdate() {
        int updated = update.getInt("postUpdate", 0);
        int current = config.getInt("postUpdate", 0);
        devLog("updated post version = "+updated, false);
        devLog("current post version = "+current, false);
        if (updated != current) {
            devLog("posts aren't updated", false);
            redirectPosts.setBackground(ContextCompat.getDrawable(this, R.drawable.linear_blue));
        }
    }

    public void autoBackup() {
        if (config.getBoolean("enableAutoBackups", false)) {
            devLog("attempting to backup", false);
            String _dest = aBackupPath+timeString("yyMMddhhmmss");
            if (!new File(_dest).exists()) mkdirs(new File(_dest));
            File[] _maps = new File(lacPath).listFiles();
            if (_maps == null) {
                devLog("file list is null", false);
            } else {
                for (int i = 0; i < _maps.length; i++) {
                    String _path = _maps[i].getPath();
                    String[] _arr = _path.split("/");
                    String _name = _arr[_arr.length - 1];
                    if (!_maps[i].isDirectory()) copyFile(_path, _dest+"/"+_name);
                }
            }
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
                devLog(e.toString(), true);
            }
        }
    }

    public void switchActivity(Class i, Boolean allowWithoutPerms) {
        if (update.getBoolean("blockAccess", false)) return;
        if (!allowWithoutPerms && !hasPerms) {
            devLog("no required permissions, checking again", false);
            checkPerms();
        } else {
            Intent intent = new Intent(this.getApplicationContext(), i);
            devLog("attempting to redirect to "+i.toString(), false);
            if (i == OptionsActivity.class) {
                startActivityForResult(intent, 5);
            } else {
                startActivity(intent);
            }
        }
    }

    public void redirectURL(String url) {
        devLog("redirecting to: "+url, false);
        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        startActivity(viewIntent);
    }

    public void copyFile(String src, String dst) {
        devLog("attempting to copy "+src+" to "+dst, false);
        try {
            FileUtil.copyFile(src, dst);
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString(), true);
        }
    }

    public void mkdirs(File mk) {
        mk.mkdirs();
        devLog("mkdirs: "+mk.getPath(), false);
    }

    public String timeString(String frmString) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat frm = new SimpleDateFormat(frmString);
        Date now = Calendar.getInstance().getTime();
        return frm.format(now);
    }

    public void checkPerms() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                hasPerms = false;
                missingPerms.setVisibility(View.VISIBLE);
                warnLinear.setVisibility(View.VISIBLE);
                devLog("permission denied, attempting to request permission", false);
                Toast.makeText(getApplicationContext(), R.string.info_storagePerm, Toast.LENGTH_SHORT).show();
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                if (Build.VERSION.SDK_INT >= 30) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    Toast.makeText(getApplicationContext(), R.string.info_storageAndroid11, Toast.LENGTH_LONG).show();
                    startActivityForResult(intent, 4);
                }

            } else {
                hasPerms = true;
                missingPerms.setVisibility(View.GONE);
                warnLinear.setVisibility(View.GONE);
                devLog("permissions granted", false);
            }
        } else {
            hasPerms = true;
            missingPerms.setVisibility(View.GONE);
            warnLinear.setVisibility(View.GONE);
            devLog("old SDK version detected", false);
        }
    }

    public void getLog() {
        if (update.getBoolean("update-available", false)) {
            devLog("update found", false);
            updateLinear.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.linear_blue));
            updateNotes.setText(update.getString("update-changelog", null));
            if (updateNotes.getText().length() < 1) updateNotes.setVisibility(View.GONE);
            updateText.setVisibility(View.VISIBLE);
            updateButton.setVisibility(View.VISIBLE);
            updateButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        redirectURL(update.getString("update-download", null));
                    }
                    AppUtil.handleOnPressEvent(v, event);
                    return true;
                }
            });
        } else {
            devLog("update not found", false);
            String noteFull = update.getString("notes", "");
            if (noteFull.length() > 0) {
                devLog("notes found", false);
                if (noteFull.contains("%BUTTON%")) {
                    updateLinear.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.linear_button));
                    noteFull = noteFull.replace("%BUTTON%", "");
                }
                if (noteFull.contains("%BLUE%")) {
                    updateLinear.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.linear_blue));
                    noteFull = noteFull.replace("%BLUE%", "");
                }
                if (noteFull.contains("%RED%")) {
                    updateLinear.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.linear_red));
                    noteFull = noteFull.replace("%RED%", "");
                }
                updateNotes.setText(noteFull);
            } else {
                devLog("notes not found", false);
                updateNotes.setVisibility(View.GONE);
                updateLinear.setVisibility(View.GONE);
            }
        }
    }

    public void devLog(String toLog, Boolean error) {
        if (devMode) {
            String tag = Thread.currentThread().getStackTrace()[3].getMethodName();
            if (error) toLog = "<font color=red>"+toLog+"</font>";
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
        if (requestCode == 4) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                int toTake = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                grantUriPermission(getApplicationContext().getPackageName(), data.getData(), toTake);
                getApplicationContext().getContentResolver().takePersistableUriPermission(data.getData(), toTake);
            }
        }
    }

    public void setListeners() {
        missingPerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPerms();
            }
        });

        optionsLinear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        redirectMaps.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    switchActivity(MapsActivity.class, false);
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        redirectWallpaper.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    switchActivity(WallpaperActivity.class, false);
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        appOptionsLinear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        redirectPosts.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    switchActivity(PostsActivity.class, true);
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        redirectOptions.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    switchActivity(OptionsActivity.class, true);
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        updateLinear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });
    }
}