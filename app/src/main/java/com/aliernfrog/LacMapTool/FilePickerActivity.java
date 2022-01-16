package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;

import java.io.File;
import java.util.Arrays;

public class FilePickerActivity extends AppCompatActivity implements PickiTCallbacks {
    Toolbar toolbar;
    TextView pathView;
    HorizontalScrollView pathScroll;
    LinearLayout goParent;
    ProgressBar progressBar;
    LinearLayout root;

    String fileTypeSaf;
    String[] fileTypeInApp;
    Boolean useInAppFilePicker;
    Boolean loadImages = false;

    SharedPreferences config;
    Handler handler = new Handler();

    String homeDir;

    Drawable icon_file;
    Drawable icon_folder;

    PickiT pickiT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_picker);

        pickiT = new PickiT(this, this, this);

        config = getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE);

        fileTypeSaf = getIntent().getStringExtra("FILE_TYPE_SAF");
        fileTypeInApp = getIntent().getStringArrayExtra("FILE_TYPE_INAPP");
        useInAppFilePicker = config.getBoolean("useInAppFilePicker", false);

        homeDir = Environment.getExternalStorageDirectory().getPath();

        icon_file = ContextCompat.getDrawable(getApplicationContext(), R.drawable.file);
        icon_folder = ContextCompat.getDrawable(getApplicationContext(), R.drawable.folder);

        toolbar = findViewById(R.id.filePicker_toolbar);
        pathView = findViewById(R.id.filePicker_path);
        pathScroll = findViewById(R.id.filePicker_path_scroll);
        goParent = findViewById(R.id.filePicker_goParent);
        progressBar = findViewById(R.id.filePicker_progressBar);
        root = findViewById(R.id.filePicker_root);

        if (!useInAppFilePicker) pickFileSaf();
        if (useInAppFilePicker) loadDir(homeDir);

        getLoadImages();
        setListeners();
    }

    public void pickFileSaf() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        if (fileTypeSaf != null) intent.setType(fileTypeSaf);
        startActivityForResult(intent, 1);
    }

    public void loadDir(String path) {
        File file = new File(path);
        File[] content = file.listFiles();
        if (content != null) {
            Arrays.sort(content);
            progressBar.setVisibility(View.VISIBLE);
            root.removeAllViews();
            pathView.setText(file.getPath());
            handler.postDelayed(() -> {
                for (File cur : content) {
                    ViewGroup view = (ViewGroup) getLayoutInflater().inflate(R.layout.inflate_file, root, false);
                    addFileView(view, cur);
                }
                progressBar.setVisibility(View.GONE);
                pathScroll.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }, 50);
        }
    }

    public void addFileView(ViewGroup view, File file) {
        ImageView iconView = view.findViewById(R.id.file_icon);
        TextView nameView = view.findViewById(R.id.file_name);
        TextView detailsView = view.findViewById(R.id.file_details);
        Drawable icon = icon_file;
        String name = file.getName();
        String details = getString(R.string.filePicker_folder);
        if (file.isDirectory()) icon = icon_folder;
        if (file.isFile() && loadImages && (name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".png"))) icon = Drawable.createFromPath(file.getPath());
        if (file.isFile()) details = (file.length()/1024)+" KB";
        iconView.setImageDrawable(icon);
        nameView.setText(name);
        detailsView.setText(details);
        AppUtil.handleOnPressEvent(view, () -> {
            if (file.isDirectory()) {
                loadDir(file.getPath());
            } else {
                checkFile(file);
            }
        });
        if (file.isFile() && checkFileExtension(file)) view.setBackgroundResource(R.drawable.linear_blue);
        root.addView(view);
    }

    public Boolean checkFileExtension(File file) {
        boolean ret = false;
        boolean hasSpecifiedType = fileTypeInApp != null && fileTypeInApp.length > 0;
        String path = file.getPath();
        if (hasSpecifiedType) {
            for (String cur : fileTypeInApp) {
                if (!cur.startsWith(".")) cur = "."+cur;
                if (path.toLowerCase().endsWith(cur)) {
                    ret = true;
                    break;
                }
            }
        } else {
            ret = true;
        }
        return ret;
    }

    public void checkFile(File file) {
        boolean allow = checkFileExtension(file);
        String path = file.getPath();
        if (!allow) Toast.makeText(getApplicationContext(), R.string.filePicker_notValid, Toast.LENGTH_SHORT).show();
        if (allow) finishGettingFile(path);
    }

    public void goParentDir() {
        String currentPath = pathView.getText().toString();
        if (currentPath.equals(homeDir)) {
            finish();
        } else {
            File currentFile = new File(currentPath);
            String parentPath = currentFile.getParent();
            loadDir(parentPath);
        }
    }

    public void getLoadImages() {
        boolean hasSpecifiedType = fileTypeInApp != null && fileTypeInApp.length > 0;
        if (!hasSpecifiedType) return;
        for (String cur : fileTypeInApp) {
            if (!cur.startsWith(".")) cur = "."+cur;
            if (cur.equals(".jpg") || cur.equals(".jpeg") || cur.equals(".png")) {
                loadImages = true;
                break;
            }
        }
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

    void setListeners() {
        toolbar.setNavigationOnClickListener(v -> goParentDir());
        AppUtil.handleOnPressEvent(goParent, this::goParentDir);
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

    @Override
    public void onBackPressed() {
        goParentDir();
    }
}