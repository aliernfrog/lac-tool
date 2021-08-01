package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aliernfrog.LacMapTool.utils.AppUtil;

public class NewMapsActivity extends AppCompatActivity {
    Button pickMap;
    TextView debugText;

    Integer REQUEST_PICK_MAP = 1;
    Integer REQUEST_PICK_THUMBNAIL = 2;
    Integer REQUEST_URI = 3;

    Integer uriSdkVersion = 29;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_maps);

        pickMap = findViewById(R.id.maps_pick_pick);
        debugText = findViewById(R.id.maps_debug);

        devLog("NewMapsActivity started");

        setListeners();
    }

    void pickFile(String fileType, Integer requestCode) {
        devLog("attempting to pick a file with request code: "+requestCode);
        Intent intent = new Intent(this, FilePickerActivity.class);
        intent.putExtra("FILE_TYPE", fileType);
        startActivityForResult(intent, requestCode);
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
        } else {
            devLog("result is not handled");
        }
    }

    void devLog(String toLog) {
        if (debugText.getVisibility() == View.VISIBLE) {
            String tag = Thread.currentThread().getStackTrace()[3].getMethodName();
            if (toLog.contains("Exception")) toLog = "<font color=red>"+toLog+"</font>";
            String log = Html.toHtml(new SpannableString(debugText.getText()));
            String full = log+"<font color=#00FFFF>["+tag+"]</font> "+toLog;
            debugText.setText(Html.fromHtml(full));
        }
    }

    void setListeners() {
        AppUtil.handleOnPressEvent(pickMap, () -> pickFile("text/*", REQUEST_PICK_MAP));
        AppUtil.handleOnPressEvent(debugText);
    }
}