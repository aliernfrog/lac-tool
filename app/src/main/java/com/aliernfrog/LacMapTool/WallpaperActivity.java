package com.aliernfrog.LacMapTool;

import androidx.annotation.RequiresApi;
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
    String wpTreePath;
    String wpPath;
    String backupPath;
    String tempPath;
    String logs = "";

    Uri lacTreeUri;
    DocumentFile lacTreeFile;

    PickiT pickiT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);

        config = getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE);
        update = getSharedPreferences("APP_UPDATE", Context.MODE_PRIVATE);
        devMode = config.getBoolean("enableDebug", false);
        wpTreePath = update.getString("path-lac", null).replace("/editor", "/wallpaper");
        wpPath = wpTreePath+"/wallpaper.jpg";
        backupPath = update.getString("path-app", null)+"wp-backup.jpg";
        tempPath = update.getString("path-app", null)+"temp/wp/";

        goback = findViewById(R.id.wallpaper_goback);
        desc = findViewById(R.id.wallpaper_desc);
        actionsLinear = findViewById(R.id.wallpaper_actionsLinear);
        pickFile = findViewById(R.id.wallpaper_pickFile);
        importFile = findViewById(R.id.wallpaper_importFile);
        wallpaperView = findViewById(R.id.wallpaper_wallpaperView);
        logView = findViewById(R.id.wallpaper_log);

        pickiT = new PickiT(this, this, this);

        if (!devMode) logView.setVisibility(View.GONE);
        devLog("==== DEBUG LOGS ====", false);
        devLog("wpPath = "+wpPath, false);
        devLog("", false);
        setListeners();

        if (Build.VERSION.SDK_INT >= 30) {
            String lacTreeId = wpTreePath.replace(Environment.getExternalStorageDirectory()+"/", "primary:");
            Uri lacUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", lacTreeId);
            lacTreeUri = DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", lacTreeId);
            int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;
            if (getApplicationContext().checkUriPermission(lacTreeUri, Process.myPid(), Process.myUid(), Intent.FLAG_GRANT_READ_URI_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                devLog("no permissions to lac data, attempting to request", false);
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

        File wpFile = new File(wpPath);
        if (wpFile.exists()) {
            Bitmap wpbitmap = BitmapFactory.decodeFile(wpFile.getAbsolutePath());
            wallpaperView.setImageBitmap(wpbitmap);
        }
    }

    public void getWp(String path) {
        rawPath = path.replace("/document/primary:", Environment.getExternalStorageDirectory().toString()+"/").replace("/document/raw:/", "");
        if (rawPath.startsWith(wpPath)) {
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
            copyFile(check.getPath(), backupPath);
        }
        copyFile(rawPath, wpPath);
        Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
    }

    public void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 2);
        devLog("attempting to pick a file with request code 2", false);
    }

    public void useTempPath() {
        lacTreeFile = DocumentFile.fromTreeUri(getApplicationContext(), lacTreeUri);
        File tempFile = new File(tempPath);
        if (!tempFile.exists()) tempFile.mkdirs();
        if (lacTreeFile != null) {
            DocumentFile[] files = lacTreeFile.listFiles();
            for (int i = 0; i < files.length; i++) {
                copyFile(files[i], tempPath+files[i].getName());
            }
        }
        wpPath = tempPath+"wallpaper.jpg";
    }

    public void copyFile(String src, String dst) {
        devLog("attempting to copy "+src+" to "+dst, false);
        try {
            FileUtil.copyFile(src, dst);
            devLog("copied from "+src+" to "+dst, false);
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString(), true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void copyFile(String src, DocumentFile dst) {
        devLog("attempting to copy "+src+" to "+dst, false);
        try {
            FileUtil.copyFile(src, dst, getApplicationContext());
            devLog("copied successfully", false);
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString(), true);
        }
    }

    public void copyFile(DocumentFile src, String dst) {
        devLog("attempting to copy "+src.getUri()+" to "+dst, false);
        try {
            FileUtil.copyFile(src, dst, getApplicationContext());
            devLog("copied successfully", false);
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString(), true);
        }
    }

    public void saveChangesAndFinish() {
        if (Build.VERSION.SDK_INT >= 30) {
            devLog("attempting to save changes", false);
            File file = new File(tempPath);
            File[] files = file.listFiles();
            try {
                for (int i = 0; i < files.length; i++) {
                    DocumentFile fileInLac = lacTreeFile.findFile(files[i].getName());
                    if (fileInLac == null) fileInLac = lacTreeFile.createFile("", files[i].getName());
                    copyFile(files[i].getPath(), fileInLac);
                }
            } finally {
                for (int i = 0; i < files.length; i++) {
                    files[i].delete();
                }
                finish();
            }
        } else {
            finish();
        }
    }

    void devLog(String toLog, Boolean error) {
        if (devMode) {
            String tag = Thread.currentThread().getStackTrace()[3].getMethodName();
            if (error) toLog = "<font color=red>"+toLog+"</font>";
            logs = logs+"<br /><font color=#00FFFF>["+tag+"]</font> "+toLog;
            logView.setText(Html.fromHtml(logs));
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
        } else if (requestCode == 1) {
            if (data == null) {
                devLog(requestCode+": no data", false);
            } else {
                if (Build.VERSION.SDK_INT >= 30) {
                    int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    grantUriPermission(getApplicationContext().getPackageName(), data.getData(), takeFlags);
                    getApplicationContext().getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
                    devLog(requestCode+": granted permissions for: "+data.getData(), false);
                    useTempPath();
                }
            }
        }
    }

    void setListeners() {
        goback.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    saveChangesAndFinish();
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
        saveChangesAndFinish();
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations()) {
            pickiT.deleteTemporaryFile(this);
            saveChangesAndFinish();
        }
    }
}