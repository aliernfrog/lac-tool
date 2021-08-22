package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.aliernfrog.LacMapTool.utils.FileUtil;

import java.io.File;

@SuppressLint("ClickableViewAccessibility")
public class RestoreActivity extends AppCompatActivity {
    ImageView goHome;
    LinearLayout mapsselect;
    Spinner mapsList;
    Button select;
    LinearLayout restoreLinear;
    TextView mapname;
    Button restore;

    SharedPreferences update;

    String backupPath;
    String mapsPath;
    String rawPath;
    String savePath;
    String mapName;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore);

        goHome = findViewById(R.id.restore_goback);
        mapsselect = findViewById(R.id.restore_mapsselect);
        mapsList = findViewById(R.id.restore_maps);
        select = findViewById(R.id.restore_selectButton);
        restoreLinear = findViewById(R.id.restore_linear);
        mapname = findViewById(R.id.restore_mapname);
        restore = findViewById(R.id.restore_backuprestore);

        update = getSharedPreferences("APP_UPDATE", Context.MODE_PRIVATE);
        backupPath = getIntent().getStringExtra("backupPath");
        mapsPath = getIntent().getStringExtra("mapsPath");

        setOnClick();
        refreshMaps();
    }

    void refreshMaps() {
        String _mapname;
        File directory = new File(backupPath);
        File[] files = directory.listFiles();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner);
        if (files != null) {
            for (File file : files) {
                _mapname = file.getName().replace(".txt", "");
                adapter.add(_mapname);
            }
        }
        mapsList.setAdapter(adapter);
    }

    void restore() {
        if (rawPath == null || rawPath.equals("")) return;
        savePath = mapsPath+"/"+mapName+".txt";
        try {
            copyFile(rawPath, savePath);
            Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void _getMap(String path) {
        rawPath = path;
        String[] arr = path.replace(".txt", "").split("/");
        mapName = arr[arr.length - 1];
        mapname.setText(mapName);
    }

    void copyFile(String src, String dst) throws Exception {
        FileUtil.copyFile(src, dst);
    }

    void setOnClick() {
        AppUtil.handleOnPressEvent(goHome, this::finish);
        AppUtil.handleOnPressEvent(mapsselect);
        AppUtil.handleOnPressEvent(select, () -> {
            if (mapsList.getSelectedItem() == null) return;
            String mapname = mapsList.getSelectedItem().toString();
            if (mapname.equals("")) return;
            _getMap(backupPath+"/"+mapname+".txt");
        });
        AppUtil.handleOnPressEvent(restoreLinear);

        mapsList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                restore.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                restore.setVisibility(View.INVISIBLE);
            }
        });

        AppUtil.handleOnPressEvent(restore, this::restore);
    }
}