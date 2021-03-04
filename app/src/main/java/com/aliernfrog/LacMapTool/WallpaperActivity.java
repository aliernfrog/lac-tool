package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.aliernfrog.LacMapTool.utils.FileUtil;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;

import java.io.File;

@SuppressLint("ClickableViewAccessibility")
public class WallpaperActivity extends AppCompatActivity implements PickiTCallbacks {
    ImageView goback;
    TextView desc;
    LinearLayout actionsLinear;
    Button pickFile;
    Button importFile;
    ImageView wallpaperView;
    TextView logView;

    SharedPreferences config;
    SharedPreferences update;

    Boolean devMode;
    String rawPath;
    String wpPath;
    String backupPath;
    String logs = "";

    PickiT pickiT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);

        config = getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE);
        update = getSharedPreferences("APP_UPDATE", Context.MODE_PRIVATE);
        devMode = config.getBoolean("enableDebug", false);
        wpPath = update.getString("path-lac", null).replace("/editor/", "/wallpaper/wallpaper.jpg");
        backupPath = update.getString("path-app", null)+"wp-backup.jpg";

        goback = findViewById(R.id.wallpaper_goback);
        desc = findViewById(R.id.wallpaper_desc);
        actionsLinear = findViewById(R.id.wallpaper_actionsLinear);
        pickFile = findViewById(R.id.wallpaper_pickFile);
        importFile = findViewById(R.id.wallpaper_importFile);
        wallpaperView = findViewById(R.id.wallpaper_wallpaperView);
        logView = findViewById(R.id.wallpaper_log);

        pickiT = new PickiT(this, this, this);

        File wpFile = new File(wpPath);
        if (wpFile.exists()) {
            Bitmap wpbitmap = BitmapFactory.decodeFile(wpFile.getAbsolutePath());
            wallpaperView.setImageBitmap(wpbitmap);
        }

        if (Build.VERSION.SDK_INT == 30) {
            desc.setBackgroundResource(R.drawable.linear_red);
            desc.setText(R.string.wallpaperDescAndroid11);
        }

        if (!devMode) logView.setVisibility(View.GONE);
        devLog("==== DEBUG LOGS ====", false);
        devLog("", false);
        setListeners();
    }

    public void getWp(String path) {
        rawPath = path.replace("/document/primary:", Environment.getExternalStorageDirectory().toString()+"/").replace("/document/raw:/", "");
        if (rawPath.startsWith(wpPath)) {
            //if imported
            importFile.setVisibility(View.GONE);
        } else {
            importFile.setVisibility(View.VISIBLE);
        }
        Bitmap wpBitmap = BitmapFactory.decodeFile(new File(rawPath).getAbsolutePath());
        wallpaperView.setImageBitmap(wpBitmap);
        devLog("rawPath: "+ rawPath, false);
    }

    public void importWp() {
        File check = new File(wpPath);
        if (check.exists()) {
            devLog("a wallpaper already exists, attempting to copy", false);
            copyFile(check.getPath(), backupPath, false);
        }
        copyFile(rawPath, wpPath, true);
        Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
    }

    public void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 2);
        devLog("attempting to pick a file with request code 2", false);
    }

    public void checkFiles() {
        File wpFile = new File(wpPath.replace("wallpaper.jpg", ""));
        File backupFile = new File(backupPath.replace("wp-backup.jpg", ""));
        if (!wpFile.exists()) wpFile.mkdirs();
        if (!backupFile.exists()) backupFile.mkdirs();
    }

    public void devLog(String toLog, Boolean error) {
        if (devMode) {
            String tag = Thread.currentThread().getStackTrace()[3].getMethodName();
            if (error) toLog = "<font color=red>"+toLog+"</font>";
            logs = logs+"<br /><font color=#00FFFF>["+tag+"]</font> "+toLog;
            logView.setText(Html.fromHtml(logs));
        }
    }

    public void copyFile(String src, String dst, Boolean getWp) {
        devLog("attempting to copy "+src+" to "+dst, false);
        try {
            FileUtil.copyFile(src, dst);
            devLog("copied from "+src+" to "+dst, false);
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString(), true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (data == null) {
                devLog("2: no data", false);
            } else {
                Uri URI = data.getData();
                File file = new File(URI.getPath());
                devLog("2: "+file.getPath(), false);
                pickiT.getPath(data.getData(), Build.VERSION.SDK_INT);
            }
        }
    }

    void setListeners() {
        goback.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    finish();
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        desc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    desc.setVisibility(View.GONE);
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        actionsLinear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        pickFile.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    pickFile();
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        importFile.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    importWp();
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        wallpaperView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });
    }

    @Override
    public void PickiTonUriReturned() {

    }

    @Override
    public void PickiTonStartListener() {

    }

    @Override
    public void PickiTonProgressUpdate(int progress) {

    }

    @Override
    public void PickiTonCompleteListener(String path, boolean wasDriveFile, boolean wasUnknownProvider, boolean wasSuccessful, String Reason) {
        if (wasSuccessful) {
            devLog("got path: "+path, false);
            getWp(path);
        } else {
            devLog(Reason, true);
        }
    }

    @Override
    public void onBackPressed() {
        pickiT.deleteTemporaryFile(this);
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations()) {
            pickiT.deleteTemporaryFile(this);
        }
    }
}