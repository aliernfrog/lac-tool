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
import android.os.StrictMode;
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
public class GalleryActivity extends AppCompatActivity {
    ImageView goback;
    TextView noScreenshots;
    LinearLayout rootLinear;
    TextView log;

    Boolean devMode;
    String logs = "";

    Integer uriSdkVersion;

    Uri lacTreeUri;
    DocumentFile lacTreeFile;
    String lacPath;
    String tempPath;

    int TREE_REQUEST_CODE = 4;

    SharedPreferences config;
    SharedPreferences update;

    @SuppressLint({"CommitPrefEdits", "InlinedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        config = getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE);
        update = getSharedPreferences("APP_UPDATE", Context.MODE_PRIVATE);

        devMode = config.getBoolean("enableDebug", false);
        uriSdkVersion = config.getInt("uriSdkVersion", 30);

        lacPath = update.getString("path-lac", null).replace("/editor", "/screenshots");
        tempPath = update.getString("path-app", null)+"temp/screenshots/";

        goback = findViewById(R.id.gallery_goback);
        noScreenshots = findViewById(R.id.gallery_noScreenshots);
        rootLinear = findViewById(R.id.gallery_linear_screenshots);
        log = findViewById(R.id.gallery_log);

        devLog("GalleryActivity started");
        devLog("uriSdkVersion: "+uriSdkVersion);

        if (Build.VERSION.SDK_INT >= uriSdkVersion) {
            String lacTreeId = lacPath.replace(Environment.getExternalStorageDirectory()+"/", "primary:");
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
                startActivityForResult(intent, TREE_REQUEST_CODE);
            } else {
                useTempPath();
            }
        } else {
            lacPath += "/";
        }
        devLog(lacPath);

        getScreenshots();
        setListeners();
    }

    public void getScreenshots() {
        devLog("attempting to get screenshots");
        File file = new File(lacPath);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                if (files.length < 1) noScreenshots.setVisibility(View.VISIBLE);
                for (File value : files) {
                    if (value.getName().endsWith(".jpg")) {
                        devLog("found: " + value.getName());
                        ViewGroup layout = (ViewGroup) getLayoutInflater().inflate(R.layout.inflate_screenshot, rootLinear, false);
                        setScreenshotView(layout, value);
                    }
                }
            }
        } else {
            devLog("screenshots file is null");
            noScreenshots.setVisibility(View.VISIBLE);
        }
    }

    public void setScreenshotView(ViewGroup layout, File file) {
        LinearLayout background = layout.findViewById(R.id.ss_bg);
        ImageView image = layout.findViewById(R.id.ss_image);
        Button share = layout.findViewById(R.id.ss_share);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        image.setImageBitmap(bitmap);
        AppUtil.handleOnPressEvent(background);
        AppUtil.handleOnPressEvent(share, () -> shareFile(file.getPath()));
        rootLinear.addView(layout);
    }

    public void saveChangesAndFinish() {
        if (Build.VERSION.SDK_INT >= 30) {
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
        lacPath = tempPath;
    }

    public void shareFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            devLog("attempting to share: "+path);
            Intent share = FileUtil.shareFile(path, "image/*", getApplicationContext());
            startActivity(Intent.createChooser(share, "Share Screenshot"));
        } else {
            Toast.makeText(getApplicationContext(), R.string.denied_doesntExist, Toast.LENGTH_SHORT).show();
            devLog("file does not exist");
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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

    void devLog(String toLog) {
        if (devMode) {
            String tag = Thread.currentThread().getStackTrace()[3].getMethodName();
            if (toLog.contains("Exception")) toLog = "<font color=red>"+toLog+"</font>";
            logs = logs+"<br /><font color=#00FFFF>["+tag+"]</font> "+toLog;
            log.setText(Html.fromHtml(logs));
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        devLog("received result for: "+requestCode);
        if (requestCode == TREE_REQUEST_CODE) {
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
        AppUtil.handleOnPressEvent(noScreenshots);
    }

    @Override
    public void onBackPressed() {
        saveChangesAndFinish();
        super.onBackPressed();
    }
}