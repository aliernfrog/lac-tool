package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.provider.DocumentsContract;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.aliernfrog.LacMapTool.utils.FileUtil;

import java.io.File;

@SuppressLint("ClickableViewAccessibility")
public class WallpaperActivity extends AppCompatActivity {
    ImageView goback;
    LinearLayout rootLayout;
    TextView desc;
    LinearLayout actionsLinear;
    Button pickFile;
    LinearLayout pickedWpLinear;
    ImageView wallpaperView;
    Button importFile;
    TextView logView;

    SharedPreferences config;
    SharedPreferences update;

    Integer uriSdkVersion;

    Boolean devMode;
    String lacPath;
    String rawPath;
    String wpTreePath;
    String wpPath;
    String tempPath;
    String logs = "";

    String wpName;

    Uri lacTreeUri;
    DocumentFile lacTreeFile;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);

        config = getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE);
        update = getSharedPreferences("APP_UPDATE", Context.MODE_PRIVATE);

        uriSdkVersion = config.getInt("uriSdkVersion", 30);
        devMode = config.getBoolean("enableDebug", false);
        wpTreePath = update.getString("path-lac", null).replace("/editor", "/wallpaper");
        lacPath = wpTreePath+"/";
        tempPath = update.getString("path-app", null)+"temp/wp/";

        goback = findViewById(R.id.wallpaper_goback);
        rootLayout = findViewById(R.id.wallpaper_rootLinear);
        desc = findViewById(R.id.wallpaper_desc);
        actionsLinear = findViewById(R.id.wallpaper_actionsLinear);
        pickFile = findViewById(R.id.wallpaper_pickFile);
        pickedWpLinear = findViewById(R.id.wallpaper_picked_linear);
        wallpaperView = findViewById(R.id.wallpaper_picked_image);
        importFile = findViewById(R.id.wallpaper_importFile);
        logView = findViewById(R.id.wallpaper_log);

        if (!devMode) logView.setVisibility(View.GONE);
        devLog("WallpaperActivity started");
        devLog("uriSdkVersion: "+uriSdkVersion);
        setListeners();

        wpPath = wpTreePath+"/";

        if (Build.VERSION.SDK_INT >= uriSdkVersion) {
            String lacTreeId = wpTreePath.replace(Environment.getExternalStorageDirectory()+"/", "primary:");
            Uri lacUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", lacTreeId);
            lacTreeUri = DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", lacTreeId);
            int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;
            if (getApplicationContext().checkUriPermission(lacTreeUri, Process.myPid(), Process.myUid(), Intent.FLAG_GRANT_READ_URI_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                devLog("no permissions to lac data, attempting to request");
                Toast.makeText(getApplicationContext(), R.string.info_treePerm, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                        .putExtra(DocumentsContract.EXTRA_INITIAL_URI, lacUri)
                        .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        .putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                        .addFlags(takeFlags);
                startActivityForResult(intent, 1);
            } else {
                useTempPath();
            }
        }

        devLog("wpPath = "+wpPath);
        devLog("");

        getImportedWallpapers();
    }

    public void getWp(String path) {
        wpName = new File(path).getName().replace(".png", ".jpg").replace(".jpeg", ".jpg");
        rawPath = path;
        pickedWpLinear.setVisibility(View.VISIBLE);
        Bitmap wpBitmap = BitmapFactory.decodeFile(new File(rawPath).getAbsolutePath());
        wallpaperView.setImageBitmap(wpBitmap);
        devLog("wpName = "+wpName);
        devLog("rawPath = "+ rawPath);
    }

    public void getImportedWallpapers() {
        devLog("attempting to get imported wallpapers");
        rootLayout.removeAllViews();
        File wpFile = new File(wpPath);
        if (wpFile.exists()) {
            File[] files = wpFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".jpg")) {
                        devLog("found: " + file.getName());
                        ViewGroup layout = (ViewGroup) getLayoutInflater().inflate(R.layout.wallpaper, rootLayout, false);
                        setWallpaperView(layout, file);
                    }
                }
            }
        } else {
            devLog("wallpaper file doesnt exist"+wpFile.getPath());
        }
    }

    public void setWallpaperView(ViewGroup layout, File file) {
        LinearLayout bg = layout.findViewById(R.id.wp_bg);
        TextView name = layout.findViewById(R.id.wp_name);
        ImageView image = layout.findViewById(R.id.wp_image);
        Button copyUrl = layout.findViewById(R.id.wp_copyUrl);
        Button delete = layout.findViewById(R.id.wp_delete);
        name.setText(file.getName());
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        image.setImageBitmap(bitmap);
        rootLayout.addView(layout);
        AppUtil.handleOnPressEvent(bg);
        AppUtil.handleOnPressEvent(copyUrl, () -> {
            AppUtil.copyToClipboard("file://"+lacPath+file.getName(), getApplicationContext());
            Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
        });
        AppUtil.handleOnPressEvent(delete, () -> {
            if (Build.VERSION.SDK_INT < 30) {
                file.delete();
                rootLayout.removeView(layout);
                Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.info_android11notAvailable, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void importWp() {
        copyFile(rawPath, wpPath+wpName);
        pickedWpLinear.setVisibility(View.GONE);
        getImportedWallpapers();
        Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
    }

    public void pickFile() {
        Intent intent = new Intent(this, FilePickerActivity.class);
        intent.putExtra("FILE_TYPE_SAF", "image/*");
        intent.putExtra("FILE_TYPE_INAPP", new String[]{"jpg","jpeg","png"});
        startActivityForResult(intent, 2);
        devLog("attempting to pick a file with request code 2");
    }

    public void useTempPath() {
        lacTreeFile = DocumentFile.fromTreeUri(getApplicationContext(), lacTreeUri);
        File tempFile = new File(tempPath);
        if (!tempFile.exists()) tempFile.mkdirs();
        if (lacTreeFile != null) {
            DocumentFile[] files = lacTreeFile.listFiles();
            for (DocumentFile file : files) {
                copyFile(file, tempPath + file.getName());
            }
        }
        wpPath = tempPath;
    }

    public void copyFile(String src, String dst) {
        devLog("attempting to copy "+src+" to "+dst);
        try {
            FileUtil.copyFile(src, dst);
            devLog("copied from "+src+" to "+dst);
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString());
        }
    }

    @SuppressLint("NewApi")
    public void copyFile(String src, DocumentFile dst) {
        devLog("attempting to copy "+src+" to "+dst);
        try {
            FileUtil.copyFile(src, dst, getApplicationContext());
            devLog("copied successfully");
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString());
        }
    }

    public void copyFile(DocumentFile src, String dst) {
        devLog("attempting to copy "+src.getUri()+" to "+dst);
        try {
            FileUtil.copyFile(src, dst, getApplicationContext());
            devLog("copied successfully");
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString());
        }
    }

    public void saveChangesAndFinish() {
        if (Build.VERSION.SDK_INT >= uriSdkVersion) {
            devLog("attempting to save changes");
            File file = new File(tempPath);
            File[] files = file.listFiles();
            try {
                if (files != null) {
                    for (File value : files) {
                        DocumentFile fileInLac = lacTreeFile.findFile(value.getName());
                        if (fileInLac == null)
                            fileInLac = lacTreeFile.createFile("", value.getName());
                        copyFile(value.getPath(), fileInLac);
                    }
                }
            } finally {
                FileUtil.deleteDirectory(file);
                finish();
            }
        } else {
            finish();
        }
    }

    void devLog(String toLog) {
        if (devMode) {
            String tag = Thread.currentThread().getStackTrace()[3].getMethodName();
            if (toLog.contains("Exception")) toLog = "<font color=red>"+toLog+"</font>";
            logs = logs+"<br /><font color=#00FFFF>["+tag+"]</font> "+toLog;
            logView.setText(Html.fromHtml(logs));
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (data == null) {
                devLog("2: no data");
            } else {
                String path = data.getStringExtra("path");
                devLog("2: "+path);
                getWp(path);
            }
        } else if (requestCode == 1) {
            if (data == null) {
                devLog(requestCode+": no data");
            } else {
                if (Build.VERSION.SDK_INT >= uriSdkVersion) {
                    int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    grantUriPermission(getApplicationContext().getPackageName(), data.getData(), takeFlags);
                    getApplicationContext().getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
                    devLog(requestCode+": granted permissions for: "+data.getData());
                    finish();
                }
            }
        }
    }

    void setListeners() {
        AppUtil.handleOnPressEvent(goback, this::saveChangesAndFinish);
        AppUtil.handleOnPressEvent(desc, () -> desc.setVisibility(View.GONE));
        AppUtil.handleOnPressEvent(actionsLinear);
        AppUtil.handleOnPressEvent(pickFile, this::pickFile);
        AppUtil.handleOnPressEvent(importFile, this::importWp);
        AppUtil.handleOnPressEvent(pickedWpLinear);
    }

    @Override
    public void onBackPressed() {
        saveChangesAndFinish();
        super.onBackPressed();
    }
}