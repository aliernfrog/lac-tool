package com.aliernfrog.LacMapTool;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;

import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.aliernfrog.LacMapTool.utils.FileUtil;

import java.io.File;

public class WallpaperActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView helpText;
    LinearLayout actionsLinear;
    Button pickWallpaperButton;
    LinearLayout pickedWallpaperLinear;
    ImageView pickedWallpaperImage;
    Button importWallpaperButton;
    LinearLayout rootLinear;
    TextView debugText;

    SharedPreferences prefsUpdate;
    SharedPreferences prefsConfig;

    Integer REQUEST_PICK_WALLPAPER = 1;
    Integer REQUEST_URI = 2;

    Integer uriSdkVersion;

    String currentPath;
    String lacPath;
    String rawLacPath;
    String tempPath;

    Uri lacTreeUri;
    DocumentFile lacTreeFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);

        toolbar = findViewById(R.id.wallpaper_toolbar);
        helpText = findViewById(R.id.wallpaper_helpText);
        actionsLinear = findViewById(R.id.wallpaper_actionsLinear);
        pickWallpaperButton = findViewById(R.id.wallpaper_pickFile);
        pickedWallpaperLinear = findViewById(R.id.wallpaper_picked_linear);
        pickedWallpaperImage = findViewById(R.id.wallpaper_picked_image);
        importWallpaperButton = findViewById(R.id.wallpaper_picked_import);
        rootLinear = findViewById(R.id.wallpaper_rootLinear);
        debugText = findViewById(R.id.wallpaper_debug);

        prefsUpdate = getSharedPreferences("APP_UPDATE", MODE_PRIVATE);
        prefsConfig = getSharedPreferences("APP_CONFIG", MODE_PRIVATE);

        lacPath = prefsUpdate.getString("path-wallpapers", "");
        rawLacPath = prefsUpdate.getString("path-wallpapers", "");
        tempPath = prefsUpdate.getString("path-temp-wallpapers", "");

        uriSdkVersion = prefsConfig.getInt("uriSdkVersion", 30);

        if (prefsConfig.getBoolean("enableDebug", false)) debugText.setVisibility(View.VISIBLE);

        devLog("WallpaperActivity started");
        devLog("uriSdkVersion: "+uriSdkVersion);
        checkUriPerms();
        setListeners();
        getImportedWallpapers();
    }

    public void getWp(String path) {
        devLog("attempting to read: "+path);
        currentPath = path;
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        pickedWallpaperImage.setImageBitmap(bitmap);
        pickedWallpaperLinear.setVisibility(View.VISIBLE);
        devLog("done reading");
    }

    public void pickWallpaperFile() {
        devLog("attempting to pick a file with request code: "+REQUEST_PICK_WALLPAPER);
        Intent intent = new Intent(this, FilePickerActivity.class);
        intent.putExtra("FILE_TYPE_SAF", "image/*");
        intent.putExtra("FILE_TYPE_INAPP", new String[]{"jpg","jpeg","png"});
        startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
    }

    public void importWallpaper() {
        devLog("attempting to import chosen wallpaper");
        File file = new File(currentPath);
        String name = FileUtil.removeExtension(file.getName());
        String dest = lacPath+"/"+name+".jpg";
        copyFile(currentPath, dest, true);
        devLog("imported the wallpaper");
        pickedWallpaperLinear.setVisibility(View.GONE);
        getImportedWallpapers();
    }

    public void getImportedWallpapers() {
        devLog("attempting to get imported wallpapers");
        rootLinear.removeAllViews();
        File lacFolder = new File(lacPath);
        File[] files = lacFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.inflate_wallpaper, rootLinear, false);
                LinearLayout bg = viewGroup.findViewById(R.id.wp_bg);
                TextView name = viewGroup.findViewById(R.id.wp_name);
                ImageView image = viewGroup.findViewById(R.id.wp_image);
                Button copyUrl = viewGroup.findViewById(R.id.wp_copyUrl);
                Button delete = viewGroup.findViewById(R.id.wp_delete);
                AppUtil.handleOnPressEvent(bg);
                name.setText(file.getName());
                image.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
                AppUtil.handleOnPressEvent(copyUrl, () -> {
                    AppUtil.copyToClipboard("file://"+rawLacPath+file.getName(), getApplicationContext());
                    Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
                });
                AppUtil.handleOnPressEvent(delete, () -> {
                    devLog(file.getPath());
                    file.delete();
                    if (Build.VERSION.SDK_INT >= uriSdkVersion) {
                        DocumentFile documentFile = lacTreeFile.findFile(file.getName());
                        if (documentFile != null) documentFile.delete();
                    }
                    rootLinear.removeView(viewGroup);
                    Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
                });
                rootLinear.addView(viewGroup);
            }
        }
    }

    public void checkUriPerms() {
        if (Build.VERSION.SDK_INT >= uriSdkVersion) {
            Intent intent = new Intent(this, UriPermActivity.class);
            intent.putExtra("path", lacPath);
            startActivityForResult(intent, REQUEST_URI);
        }
    }

    public void useTempPath() {
        lacTreeFile = DocumentFile.fromTreeUri(getApplicationContext(), lacTreeUri);
        if (lacTreeFile != null) {
            DocumentFile[] files = lacTreeFile.listFiles();
            for (DocumentFile file : files) {
                copyFile(file, tempPath + "/" + file.getName());
            }
        }
        lacPath = tempPath;
    }

    public void copyFile(String source, String destination, Boolean toastResult) {
        devLog("attempting to copy "+source+" to "+destination);
        try {
            FileUtil.copyFile(source, destination);
            devLog("copied successfully");
            if (toastResult) Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString());
            if (toastResult) Toast.makeText(getApplicationContext(), R.string.info_error, Toast.LENGTH_SHORT).show();
        }
    }

    public void copyFile(DocumentFile source, String destination) {
        devLog("attempting to copy "+source.getUri()+" to "+destination);
        try {
            FileUtil.copyFile(source, destination, getApplicationContext());
            devLog("copied successfully");
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

    public void saveChangesAndFinish() {
        if (Build.VERSION.SDK_INT >= uriSdkVersion) {
            devLog("attempting to save changes and finish");
            File tempFile = new File(tempPath);
            File[] files = tempFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    DocumentFile fileInLac = lacTreeFile.findFile(file.getName());
                    if (fileInLac == null) fileInLac = lacTreeFile.createFile("", file.getName());
                    copyFile(file.getPath(), fileInLac);
                }
            }
            FileUtil.deleteDirectoryContent(tempFile);
        }
        finish();
    }

    void devLog(String toLog) {
        AppUtil.devLog(toLog, debugText);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean hasData = data != null;
        devLog(requestCode+": hasData = "+hasData);
        if (!hasData) return;
        if (requestCode == REQUEST_PICK_WALLPAPER) {
            getWp(data.getStringExtra("path"));
        } else if (requestCode == REQUEST_URI) {
            lacTreeUri = data.getParcelableExtra("uri");
            useTempPath();
        } else {
            devLog("result is not handled");
        }
    }

    void setListeners() {
        toolbar.setNavigationOnClickListener(v -> saveChangesAndFinish());
        AppUtil.handleOnPressEvent(helpText, () -> helpText.setVisibility(View.GONE));
        AppUtil.handleOnPressEvent(actionsLinear);
        AppUtil.handleOnPressEvent(pickWallpaperButton, this::pickWallpaperFile);
        AppUtil.handleOnPressEvent(pickedWallpaperLinear);
        AppUtil.handleOnPressEvent(importWallpaperButton, this::importWallpaper);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveChangesAndFinish();
    }
}
