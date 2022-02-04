package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliernfrog.LacMapTool.fragments.MapDownloadSheet;
import com.aliernfrog.LacMapTool.fragments.MapPickerSheet;
import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.aliernfrog.LacMapTool.utils.FileUtil;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MapsMergeActivity extends AppCompatActivity implements MapPickerSheet.MapPickerListener, MapDownloadSheet.MapDownloadListener {
    Toolbar toolbar;
    LinearLayout baseMapLinear;
    TextView baseMapName;
    LinearLayout mapToAddLinear;
    TextView mapToAddName;
    TextInputEditText mapToAddPos;
    LinearLayout outputLinear;
    TextInputEditText outputMapName;
    Button startMerge;
    TextView debugText;

    SharedPreferences prefsConfig;

    String mapsPath;
    String primaryMapPath = "";
    String secondaryMapPath = "";
    String currentPickRequest;

    final String PRIMARY_MAP = "primary";
    final String SECONDARY_MAP = "secondary";
    final int REQUEST_PICK_MAP = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_merge);

        mapsPath = getIntent().getStringExtra("mapsPath");

        toolbar = findViewById(R.id.mapsMerge_toolbar);
        baseMapLinear = findViewById(R.id.mapsMerge_baseMap_linear);
        baseMapName = findViewById(R.id.mapsMerge_baseMap_name);
        mapToAddLinear = findViewById(R.id.mapsMerge_mapToAdd_linear);
        mapToAddName = findViewById(R.id.mapsMerge_mapToAdd_name);
        mapToAddPos = findViewById(R.id.mapsMerge_mapToAdd_pos);
        outputLinear = findViewById(R.id.mapsMerge_output_linear);
        outputMapName = findViewById(R.id.mapsMerge_output_name);
        startMerge = findViewById(R.id.mapsMerge_output_merge);
        debugText = findViewById(R.id.mapsMerge_debug);

        prefsConfig = getSharedPreferences("APP_CONFIG", MODE_PRIVATE);
        if (prefsConfig.getBoolean("enableDebug", false)) debugText.setVisibility(View.VISIBLE);

        devLog("MapsMergeActivity started");

        setListeners();
    }

    public void checkStuffAndStartMerging() {
        if (primaryMapPath.equals("") || secondaryMapPath.equals("")) {
            Toast.makeText(getApplicationContext(), R.string.mapMerge_warning_noMapSelected, Toast.LENGTH_SHORT).show();
        } else if (getMapToAddPosition() == null) {
            Toast.makeText(getApplicationContext(), R.string.mapMerge_warning_posNotValid, Toast.LENGTH_SHORT).show();
        } else if (getOutputName() == null) {
            Toast.makeText(getApplicationContext(), R.string.mapMerge_warning_noOutputName, Toast.LENGTH_SHORT).show();
        } else {
            File check = new File(mapsPath+"/"+getOutputName()+".txt");
            devLog("checking path: "+check.getPath());
            if (check.exists()) {
                Toast.makeText(getApplicationContext(), R.string.denied_alreadyExists, Toast.LENGTH_SHORT).show();
            } else {
                devLog("passed check, map to add pos: "+ TextUtils.join(",", getMapToAddPosition()));
                mergeMap();
            }
        }
    }

    public void mergeMap() {
        try {
            String primaryMapRaw = FileUtil.readFile(primaryMapPath);
            String secondaryMapRaw = FileUtil.readFile(secondaryMapPath);
            ArrayList<String> primaryMapLines = new ArrayList<>(Arrays.asList(primaryMapRaw.split("\n")));
            ArrayList<String> secondaryMapLines = new ArrayList<>(Arrays.asList(secondaryMapRaw.split("\n")));
            for (int i = 0; i < secondaryMapLines.size(); i++) {
                String line = secondaryMapLines.get(i);
                if (line.split(":").length > 1 && line.split(":")[0].endsWith("_Editor")) {
                    //editor object
                    primaryMapLines.add(getNewObjectPos(line));
                } else if (line.startsWith("Vehicle_")) {
                    //vehicle
                    primaryMapLines.add(getNewVehiclePos(line));
                } else if (line.startsWith("Downloadable_Content_Material")) {
                    //downloadable material
                    primaryMapLines.add(line);
                }
            }
            saveMergedMap(TextUtils.join("\n", primaryMapLines));
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString());
        }
    }

    public void saveMergedMap(String content) {
        try {
            String fileName = getOutputName() + ".txt";
            devLog("attempting to save merged map with name: " + fileName);
            FileUtil.saveFile(mapsPath, fileName, content);
            Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            devLog(e.toString());
        }
    }

    public void getMap(String path) {
        devLog(currentPickRequest+": setting path: "+path);
        File file = new File(path);
        String name = FileUtil.removeExtension(file.getName());
        if (currentPickRequest.equals(PRIMARY_MAP)) {
            primaryMapPath = path;
            baseMapName.setText(name);
        } else if (currentPickRequest.equals(SECONDARY_MAP)) {
            secondaryMapPath = path;
            mapToAddName.setText(name);
        }
    }

    void pickMap(String request) {
        currentPickRequest = request;
        Bundle bundle = new Bundle();
        bundle.putString("mapsPath", mapsPath);
        MapPickerSheet mapPickerSheet = new MapPickerSheet();
        mapPickerSheet.setArguments(bundle);
        mapPickerSheet.show(getSupportFragmentManager(), "map_pick");
    }

    String getNewObjectPos(String line) {
        String[] lineSplit = line.split(":");
        String posStr = lineSplit[1];
        String newPosStr = getNewPos(posStr, ",");
        lineSplit[1] = newPosStr;
        return TextUtils.join(":", lineSplit);
    }

    String getNewVehiclePos(String line) {
        String[] lineSplit = line.split(":");
        String posStr = lineSplit[1];
        String newPosStr = getNewPos(posStr, ", ");
        lineSplit[1] = newPosStr;
        return TextUtils.join(":", lineSplit);
    }

    String getNewPos(String str, String joinWith) {
        String[] posSplit = str.split(joinWith);
        String[] posToAddSplit = getMapToAddPosition();
        Double posX = Double.parseDouble(posSplit[0]);
        Double posY = Double.parseDouble(posSplit[1]);
        Double posZ = Double.parseDouble(posSplit[2]);
        Double posToAddX = Double.parseDouble(posToAddSplit[0]);
        Double posToAddY = Double.parseDouble(posToAddSplit[1]);
        Double posToAddZ = Double.parseDouble(posToAddSplit[2]);
        double newX = posX+posToAddX;
        double newY = posY+posToAddY;
        double newZ = posZ+posToAddZ;
        return newX+joinWith+newY+joinWith+newZ;
    }

    String getOutputName() {
        if (outputMapName.getText() == null) return null;
        String name = outputMapName.getText().toString();
        if (name.equals("")) return null;
        return name;
    }

    String[] getMapToAddPosition() {
        if (mapToAddPos.getText() == null) return null;
        String rawPos = mapToAddPos.getText().toString();
        String[] split = rawPos.split(",");
        if (split.length != 3) return null;
        boolean isNumber = true;
        for (String str : split) {
            Boolean strIsNumber = AppUtil.stringIsNumber(str);
            if (!strIsNumber) isNumber = false;
        }
        if (!isNumber) return null;
        return split;
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
        if (requestCode == REQUEST_PICK_MAP) {
            String path = data.getStringExtra("path");
            devLog("received path: "+path);
            getMap(path);
        }
    }

    void setListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());
        AppUtil.handleOnPressEvent(baseMapLinear, () -> pickMap(PRIMARY_MAP));
        AppUtil.handleOnPressEvent(mapToAddLinear, () -> pickMap(SECONDARY_MAP));
        AppUtil.handleOnPressEvent(outputLinear);
        AppUtil.handleOnPressEvent(startMerge, this::checkStuffAndStartMerging);
    }

    @Override
    public void onMapPicked(String path) {
        getMap(path);
    }

    @Override
    public void onFilePickRequested() {
        devLog("attempting to pick a map file");
        Intent intent = new Intent(this, FilePickerActivity.class);
        intent.putExtra("FILE_TYPE_SAF", "text/*");
        intent.putExtra("FILE_TYPE_INAPP", new String[]{"txt"});
        startActivityForResult(intent, REQUEST_PICK_MAP);
    }

    @Override
    public void onMapDownloaded(String path) {
        getMap(path);
    }
}