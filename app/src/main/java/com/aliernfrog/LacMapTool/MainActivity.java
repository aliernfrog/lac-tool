package com.aliernfrog.LacMapTool;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliernfrog.LacMapTool.fragments.OkCancelSheet;
import com.aliernfrog.LacMapTool.utils.AppUtil;

import java.io.File;

@SuppressLint({"CommitPrefEdits", "ClickableViewAccessibility"})
public class MainActivity extends AppCompatActivity implements OkCancelSheet.OkCancelListener {
    LinearLayout missingLac;
    LinearLayout missingPerms;
    LinearLayout lacLinear;
    LinearLayout redirectMaps;
    LinearLayout redirectWallpapers;
    LinearLayout redirectScreenshots;
    LinearLayout appLinear;
    LinearLayout startLac;
    LinearLayout checkUpdates;
    LinearLayout redirectOptions;
    LinearLayout updateLinear;
    TextView updateLinearTitle;
    TextView updateLog;
    TextView log;

    Integer REQUEST_URI = 1;

    Boolean hasPerms;
    Integer uriSdkVersion;
    Integer version;

    SharedPreferences update;
    SharedPreferences config;

    String mapsPath;
    String wallpapersPath;
    String screenshotsPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        update = getSharedPreferences("APP_UPDATE", Context.MODE_PRIVATE);
        config = getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE);
        uriSdkVersion = config.getInt("uriSdkVersion", 30);
        version = update.getInt("versionCode", 0);
        mapsPath = update.getString("path-maps", "");
        wallpapersPath = update.getString("path-wallpapers", "");
        screenshotsPath = update.getString("path-screenshots", "");

        missingLac = findViewById(R.id.main_missingLac);
        missingPerms = findViewById(R.id.main_missingPerms);
        lacLinear = findViewById(R.id.main_optionsLac);
        redirectMaps = findViewById(R.id.main_maps);
        redirectWallpapers = findViewById(R.id.main_wallpapers);
        redirectScreenshots = findViewById(R.id.main_screenshots);
        appLinear = findViewById(R.id.main_optionsApp);
        startLac = findViewById(R.id.main_startLac);
        checkUpdates = findViewById(R.id.main_checkUpdates);
        redirectOptions = findViewById(R.id.main_options);
        updateLinear = findViewById(R.id.main_update);
        updateLinearTitle = findViewById(R.id.main_update_title);
        updateLog = findViewById(R.id.main_update_description);
        log = findViewById(R.id.main_log);

        if (config.getBoolean("enableDebug", false)) log.setVisibility(View.VISIBLE);
        devLog("MainActivity started");
        devLog("uriSdkVersion: "+uriSdkVersion);

        if (!AppUtil.isLacInstalled(getApplicationContext())) {
            devLog("lac wasnt found");
            missingLac.setVisibility(View.VISIBLE);
            startLac.setVisibility(View.GONE);
        }

        checkUpdates(false);
        checkPerms();
        setListeners();
    }

    public void launchLac() {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(AppUtil.getLacId(getApplicationContext()));
        finish();
        startActivity(intent);
    }

    public void getUpdates() {
        devLog("attempting to get updates from website");
        try {
            if (AppUtil.getUpdates(getApplicationContext())) checkUpdates(true);
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString());
        }
    }

    public void checkUpdates(Boolean toastResult) {
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
            updateLinear.setBackgroundResource(R.drawable.linear_blue_light);
            AppUtil.handleOnPressEvent(updateLinear, () -> redirectURL(download));
            if (toastResult) Toast.makeText(getApplicationContext(), R.string.update_toastAvailable, Toast.LENGTH_SHORT).show();
        } else {
            if (notes != null && !notes.equals("")) {
                linearVisible = true;
                full = notes;
            }
            AppUtil.handleOnPressEvent(updateLinear);
            if (toastResult) Toast.makeText(getApplicationContext(), R.string.update_toastNoUpdates, Toast.LENGTH_SHORT).show();
        }
        updateLog.setText(Html.fromHtml(full));
        if (linearVisible) updateLinear.setVisibility(View.VISIBLE);
    }

    public void checkPerms() {
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 30) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                afterPermsDenied();
                devLog("permission denied, attempting to request permission");
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
            } else {
                devLog("permissions granted");
                afterPermsGranted();
            }
        } else if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                afterPermsDenied();
                devLog("not external storage manager, showing all file access dialog");
                showAllFilesAccessDialog();
            } else {
                devLog("is external storage manager");
                afterPermsGranted();
            }
        } else {
            devLog("old SDK version detected");
            afterPermsGranted();
        }
    }

    void showAllFilesAccessDialog() {
        Bundle bundle = new Bundle();
        bundle.putString("text", getString(R.string.info_storagePermSdk30));
        OkCancelSheet okCancelSheet = new OkCancelSheet();
        okCancelSheet.setArguments(bundle);
        okCancelSheet.show(getSupportFragmentManager(), "allfiles");
    }

    void afterPermsGranted() {
        hasPerms = true;
        missingPerms.setVisibility(View.GONE);
        createFiles();
    }

    void afterPermsDenied() {
        hasPerms = false;
        missingPerms.setVisibility(View.VISIBLE);
    }

    public void createFiles() {
        try {
            File mapsFolder = new File(update.getString("path-maps", ""));
            File wallpapersFolder = new File(update.getString("path-wallpapers", ""));
            File screenshotsFolder = new File(update.getString("path-screenshots", ""));
            File appFolder = new File(update.getString("path-app", ""));
            File backupFolder = new File(appFolder.getPath()+"/backups/");
            File aBackupFolder = new File(appFolder.getPath()+"/auto-backups/");
            File tempMapsFolder = new File(update.getString("path-temp-maps", ""));
            File tempWallpapersFolder = new File(update.getString("path-temp-wallpapers", ""));
            File tempScreenshotsFolder = new File(update.getString("path-temp-screenshots", ""));
            File nomedia = new File(appFolder.getPath()+"/.nomedia");
            if (!mapsFolder.exists()) mkdirs(mapsFolder);
            if (!wallpapersFolder.exists()) mkdirs(wallpapersFolder);
            if (!screenshotsFolder.exists()) mkdirs(screenshotsFolder);
            if (!appFolder.exists()) mkdirs(appFolder);
            if (!backupFolder.exists()) mkdirs(backupFolder);
            if (!aBackupFolder.exists()) mkdirs(aBackupFolder);
            if (!tempMapsFolder.exists()) mkdirs(tempMapsFolder);
            if (!tempWallpapersFolder.exists()) mkdirs(tempWallpapersFolder);
            if (!tempScreenshotsFolder.exists()) mkdirs(tempScreenshotsFolder);
            if (!nomedia.exists()) nomedia.createNewFile();
        } catch (Exception e) {
            devLog(e.toString());
        }
    }

    public void mkdirs(File mk) {
        boolean state = mk.mkdirs();
        devLog(mk.getPath()+" //"+state);
    }

    @SuppressLint("NewApi")
    public Boolean checkUriPerms(@Nullable String path) {
        if (Build.VERSION.SDK_INT < uriSdkVersion) return true;
        if (path == null) return true;
        String treeId = path.replace(Environment.getExternalStorageDirectory()+"/", "primary:");
        Uri uri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", treeId);
        Uri treeUri = DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", treeId);
        devLog("checking uri permissions: "+treeId);
        int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;
        if (getApplicationContext().checkUriPermission(treeUri, Process.myPid(), Process.myUid(), Intent.FLAG_GRANT_READ_URI_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            devLog("permissions not granted, requesting");
            Toast.makeText(getApplicationContext(), R.string.info_treePerm, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    .putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
                    .putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                    .addFlags(takeFlags);
            startActivityForResult(intent, REQUEST_URI);
            return false;
        } else {
            devLog("permissions granted");
            return true;
        }
    }

    public void switchActivity(Class i, Boolean allowWithoutPerms, @Nullable String path) {
        if (!allowWithoutPerms && !hasPerms) {
            devLog("no required permissions, checking again");
            checkPerms();
        } else {
            Intent intent = new Intent(this.getApplicationContext(), i);
            devLog("attempting to redirect to "+i.toString());
            if (checkUriPerms(path)) startActivity(intent);
        }
    }

    public void redirectURL(String url) {
        devLog("attempting to redirect to:"+url);
        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        startActivity(viewIntent);
    }

    void devLog(String toLog) {
        AppUtil.devLog(toLog, log);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        devLog(requestCode+": hasData = "+(data != null));
        if (requestCode == REQUEST_URI && data != null) {
            int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            grantUriPermission(getApplicationContext().getPackageName(), data.getData(), takeFlags);
            getApplicationContext().getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
            devLog("took uri permissions");
        }
    }

    public void setListeners() {
        AppUtil.handleOnPressEvent(missingLac, () -> redirectURL("https://play.google.com/store/apps/details?id=com.MA.LAC"));
        AppUtil.handleOnPressEvent(missingPerms, this::checkPerms);
        AppUtil.handleOnPressEvent(lacLinear);
        AppUtil.handleOnPressEvent(redirectMaps, () -> switchActivity(MapsActivity.class, false, mapsPath));
        AppUtil.handleOnPressEvent(redirectWallpapers, () -> switchActivity(WallpaperActivity.class, false, wallpapersPath));
        AppUtil.handleOnPressEvent(redirectScreenshots, () -> switchActivity(ScreenshotsActivity.class, false, screenshotsPath));
        AppUtil.handleOnPressEvent(appLinear);
        AppUtil.handleOnPressEvent(startLac, this::launchLac);
        AppUtil.handleOnPressEvent(checkUpdates, this::getUpdates);
        AppUtil.handleOnPressEvent(redirectOptions, () -> switchActivity(OptionsActivity.class, true, null));
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onOkClick() {
        devLog("clicked ok, requesting all files access");
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}