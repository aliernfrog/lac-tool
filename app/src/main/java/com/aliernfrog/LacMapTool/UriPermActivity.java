package com.aliernfrog.LacMapTool;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Toast;

@SuppressLint("NewApi")
public class UriPermActivity extends AppCompatActivity {
    SharedPreferences config;
    Integer uriSdkVersion;
    String path;

    int REQUEST_URI = 1;

    Uri treeUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        config = getSharedPreferences("APP_CONFIG", MODE_PRIVATE);
        uriSdkVersion = config.getInt("uriSdkVersion", 30);
        path = getIntent().getStringExtra("path");

        if (Build.VERSION.SDK_INT < uriSdkVersion) finish();
        if (path == null) finish();

        requestPerm();
    }

    void requestPerm() {
        String treeId = path.replace(Environment.getExternalStorageDirectory()+"/", "primary:");
        Uri uri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", treeId);
        treeUri = DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", treeId);
        int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;
        if (getApplicationContext().checkUriPermission(treeUri, Process.myPid(), Process.myUid(), Intent.FLAG_GRANT_READ_URI_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), R.string.info_treePerm, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    .putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
                    .putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                    .addFlags(takeFlags);
            startActivityForResult(intent, REQUEST_URI);
        }
    }

    void finishChecking() {
        Intent intent = new Intent();
        intent.putExtra("uri", treeUri);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_URI && data != null) {
            int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            grantUriPermission(getApplicationContext().getPackageName(), data.getData(), takeFlags);
            getApplicationContext().getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
            finishChecking();
        }
    }
}