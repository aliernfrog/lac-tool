package com.aliernfrog.LacMapTool;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NewMapsActivity extends AppCompatActivity {
    FloatingActionButton saveButton;
    Button pickMap;
    TextView debugText;

    SharedPreferences prefsUpdate;
    SharedPreferences prefsConfig;

    Integer REQUEST_PICK_MAP = 1;
    Integer REQUEST_PICK_THUMBNAIL = 2;
    Integer REQUEST_URI = 3;

    Integer uriSdkVersion = 29;

    String lacPath;

    Uri lacTreeUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_maps);

        saveButton = findViewById(R.id.maps_save);
        pickMap = findViewById(R.id.maps_pick_pick);
        debugText = findViewById(R.id.maps_debug);

        prefsUpdate = getSharedPreferences("APP_UPDATE", MODE_PRIVATE);
        prefsConfig = getSharedPreferences("APP_CONFIG", MODE_PRIVATE);

        lacPath = prefsUpdate.getString("path-maps", null);

        if (prefsConfig.getBoolean("enableDebug", false)) debugText.setVisibility(View.VISIBLE);

        devLog("NewMapsActivity started");

        checkUriPerms();
        setListeners();
    }

    public void pickFile(String fileType, Integer requestCode) {
        devLog("attempting to pick a file with request code: "+requestCode);
        Intent intent = new Intent(this, FilePickerActivity.class);
        intent.putExtra("FILE_TYPE", fileType);
        startActivityForResult(intent, requestCode);
    }

    @SuppressLint("NewApi")
    void checkUriPerms() {
        if (Build.VERSION.SDK_INT >= uriSdkVersion) {
            saveButton.setVisibility(View.VISIBLE);
            String treeId = lacPath.replace(Environment.getExternalStorageDirectory()+"/", "primary:");
            Uri uri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", treeId);
            lacTreeUri = DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", treeId);
            int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;
            if (getApplicationContext().checkUriPermission(lacTreeUri, Process.myPid(), Process.myUid(), Intent.FLAG_GRANT_READ_URI_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                devLog("no permissions to lac data, attempting to request");
                Toast.makeText(getApplicationContext(), R.string.info_treePerm, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                        .putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
                        .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        .putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                        .addFlags(takeFlags);
                startActivityForResult(intent, REQUEST_URI);
            }
        }
    }

    void devLog(String toLog) {
        AppUtil.devLog(toLog, debugText);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean hasData = data != null;
        devLog(requestCode+": received result");
        devLog(requestCode+": hasData = "+hasData);
        if (requestCode == REQUEST_PICK_MAP) {
            if (hasData) {
                devLog(data.getStringExtra("path"));
            } else {
                devLog("no data");
            }
        } else if (requestCode == REQUEST_URI) {
            if (Build.VERSION.SDK_INT >= 30 && hasData) {
                int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                grantUriPermission(getApplicationContext().getPackageName(), data.getData(), takeFlags);
                getApplicationContext().getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
                devLog(requestCode+": granted permissions for: "+data.getData());
                finish();
            } else {
                devLog("no data");
            }
        } else {
            devLog("result is not handled");
        }
    }

    void setListeners() {
        AppUtil.handleOnPressEvent(pickMap, () -> pickFile("text/*", REQUEST_PICK_MAP));
        AppUtil.handleOnPressEvent(debugText);
    }
}