package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;

public class FilePickerActivity extends AppCompatActivity implements PickiTCallbacks {
    String fileType;

    PickiT pickiT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_picker);

        fileType = getIntent().getStringExtra("FILE_TYPE");

        pickiT = new PickiT(this, this, this);

        pickFile();
    }

    public void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        if (fileType != null) intent.setType(fileType);
        startActivityForResult(intent, 1);
    }

    public void finishGettingFile(String path) {
        Intent intent = new Intent();
        intent.putExtra("path", path);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            pickiT.getPath(data.getData(), Build.VERSION.SDK_INT);
        } else {
            finish();
        }
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
        finishGettingFile(path);
    }
}