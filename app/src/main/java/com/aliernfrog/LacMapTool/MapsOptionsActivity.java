package com.aliernfrog.LacMapTool;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aliernfrog.LacMapTool.fragments.MapTypeSheet;
import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.aliernfrog.LacMapTool.utils.FileUtil;
import com.aliernfrog.LacMapTool.utils.LacMapUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileWriter;

@SuppressLint({"ClickableViewAccessibility", "UseSwitchCompatOrMaterialCode"})
public class MapsOptionsActivity extends AppCompatActivity implements MapTypeSheet.MapTypeListener {
    Toolbar toolbar;
    TextView mapName;
    LinearLayout serverNameLinear;
    EditText serverName;
    LinearLayout mapTypeLinear;
    Button mapTypeButton;
    LinearLayout optionsLinear;
    LinearLayout rolesLinear;
    Button editRolesButton;
    Button removeAllTdm;
    Button removeAllRace;
    Button fixMapButton;
    FloatingActionButton saveChanges;
    TextView debugText;

    SharedPreferences config;

    String rawPath;

    String[] updatedContent;

    Integer REQUEST_ROLES = 1;
    Integer LINE_SERVER_NAME;
    Integer LINE_MAP_TYPE;
    Integer LINE_ROLES;
    Integer mapTypeInt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_options);

        config = getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE);
        rawPath = getIntent().getStringExtra("path");
        if (rawPath == null) finish();

        toolbar = findViewById(R.id.mapsOptions_toolbar);
        mapName = findViewById(R.id.mapsOptions_mapName);
        serverNameLinear = findViewById(R.id.mapsOptions_serverName_linear);
        serverName = findViewById(R.id.mapsOptions_serverName_input);
        mapTypeLinear = findViewById(R.id.mapsOptions_mapType_linear);
        mapTypeButton = findViewById(R.id.mapsOptions_mapType_change);
        optionsLinear = findViewById(R.id.mapsOptions_options_linear);
        rolesLinear = findViewById(R.id.mapsOptions_roles_linear);
        editRolesButton = findViewById(R.id.mapsOptions_roles_editRoles);
        removeAllTdm = findViewById(R.id.mapsOptions_removeAll_tdm);
        removeAllRace = findViewById(R.id.mapsOptions_removeAll_race);
        fixMapButton = findViewById(R.id.mapsOptions_fix_button);
        saveChanges = findViewById(R.id.mapsOptions_save_button);
        debugText = findViewById(R.id.mapsOptions_log);

        if (config.getBoolean("enableDebug", false)) debugText.setVisibility(View.VISIBLE);

        getMap(rawPath);
        setListeners();
    }

    public void getMap(String path) {
        rawPath = path.replace("/document/primary:", Environment.getExternalStorageDirectory().toString()+"/").replace("/document/raw:/", "");
        String[] arr = path.replace(".txt", "").split("/");
        mapName.setText(arr[arr.length - 1]);
        devLog("rawPath: "+ rawPath);
        devLog("mapName: "+ mapName.getText());
        readMap(path);
    }

    public void readMapLines() {
        devLog("attempting to read map lines");
        for (int i = 0; i < updatedContent.length; i++) {
            String cur = updatedContent[i];
            String type = "option";
            if (cur.split(":").length > 1 && cur.split(":")[0].endsWith("_Editor")) type = "object";
            if (cur.startsWith("Map Name:")) type = "serverName";
            if (cur.startsWith("Map Type:")) type = "mapType";
            if (cur.startsWith("Map Logo:")) type = "serverLogo";
            if (cur.startsWith("Spawn Point:")) type = "spawnPoint";
            if (cur.startsWith("Holo Sign:")) type = "holoSign";
            if (cur.startsWith("Camera Pos:")) type = "cameraPos";
            if (cur.startsWith("Roles List:")) type = "rolesList";
            if (cur.startsWith("Vehicle_")) type = "vehicle";
            if (cur.startsWith("Downloadable_Content_Material")) type = "material";
            switch(type) {
                case "serverName":
                    getServerName(i);
                    break;
                case "mapType":
                    getMapType(i);
                    break;
                case "rolesList":
                    readRoles(i);
                case "option":
                    if (!cur.startsWith("Roles List:")) readOption(i);
            }
        }
    }

    public void readOption(Integer line) {
        String str = updatedContent[line];
        String[] split = str.split(": ");
        String title = split[0];
        String value = split[1];
        Boolean isNumber = AppUtil.stringIsNumber(value);
        if (isNumber) {
            addNumberOption(line, title, value);
        } else {
            Boolean bool = value.contains("true") || value.contains("enabled");
            addBoolOption(line, title, bool);
        }
        optionsLinear.setVisibility(View.VISIBLE);
    }

    public void addNumberOption(Integer line, String title, String value) {
        ViewGroup view = (ViewGroup) getLayoutInflater().inflate(R.layout.inflate_option_number, optionsLinear, false);
        TextInputLayout textInputLayout = view.findViewById(R.id.option_number_layout);
        TextInputEditText textInputEditText = view.findViewById(R.id.option_number_input);
        textInputLayout.setHint(title);
        textInputEditText.setText(value);
        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                setString(line, textInputEditText.getText());
            }
        });
        optionsLinear.addView(view);
    }

    public void addBoolOption(Integer line, String title, Boolean value) {
        View view = getLayoutInflater().inflate(R.layout.inflate_option_bool, optionsLinear, false);
        Switch switchView = view.findViewById(R.id.option_bool_switch);
        switchView.setText(title);
        switchView.setChecked(value);
        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> setBoolean(line, isChecked));
        optionsLinear.addView(view);
    }

    public void getServerName(Integer line) {
        LINE_SERVER_NAME = line;
        String str = updatedContent[line].replace("Map Name:","");
        serverName.setText(str);
        serverNameLinear.setVisibility(View.VISIBLE);
    }

    public void getMapType(Integer line) {
        LINE_MAP_TYPE = line;
        String str = updatedContent[line].replace("Map Type:", "");
        mapTypeInt = Integer.parseInt(str);
        mapTypeButton.setText(LacMapUtil.getMapTypeFromInt(mapTypeInt, getApplicationContext()));
        mapTypeLinear.setVisibility(View.VISIBLE);
    }

    public int getMapTypeInt() {
        return mapTypeInt;
    }

    public void setServerName(String name) {
        devLog("attempting to change server name to: "+name);
        updatedContent[LINE_SERVER_NAME] = "Map Name:"+name;
    }

    public void setMapType(Integer mapTypeNumber) {
        devLog("attempting to change map type to: "+mapTypeNumber);
        mapTypeInt = mapTypeNumber;
        updatedContent[LINE_MAP_TYPE] = "Map Type:"+mapTypeNumber.toString();
        mapTypeButton.setText(LacMapUtil.getMapTypeFromInt(mapTypeNumber, getApplicationContext()));
    }

    public void setBoolean(Integer line, Boolean bool) {
        String str = updatedContent[line];
        String[] split = str.split(":");
        String title = split[0];
        String value = split[1];
        boolean useEnabledDisabled = value.contains("enabled") || value.contains("disabled");
        String valueStr = "false";
        if (bool) valueStr = "true";
        if (useEnabledDisabled) valueStr = valueStr.replace("true","enabled").replace("false","disabled");
        setLine(line, title+": "+valueStr);
    }

    public void setString(Integer line, Object value) {
        value = value.toString();
        String str = updatedContent[line];
        String[] split = str.split(":");
        String title = split[0];
        setLine(line, title+": "+value);
    }

    public void setLine(Integer line, String string) {
        devLog("attempting to change line "+line+" to: "+string);
        updatedContent[line] = string;
    }

    public void readMap(String path) {
        File mapFile = new File(path);
        if (mapFile.exists()) {
            devLog("attempting to read: "+path);
            try {
                String _full = FileUtil.readFile(path);
                updatedContent = _full.split("\n");
                readMapLines();
            } catch (Exception e) {
                devLog(e.toString());
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.denied_doesntExist, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void readRoles(Integer line) {
        LINE_ROLES = line;
        rolesLinear.setVisibility(View.VISIBLE);
    }

    public void editRoles() {
        Intent intent = new Intent(this, MapsRolesActivity.class);
        intent.putExtra("roles", updatedContent[LINE_ROLES].replace("Roles List:",""));
        startActivityForResult(intent, REQUEST_ROLES);
    }

    public void removeAllObjects(String object) {
        devLog("attempting to remove all objects with name: "+object);
        StringBuilder newContent = new StringBuilder();
        for (int i = 0; i < updatedContent.length; i++) {
            if (!updatedContent[i].startsWith(object)) {
                newContent.append(updatedContent[i]).append("\n");
            } else {
                devLog("found "+object+" at "+i);
            }
        }
        updatedContent = newContent.toString().split("\n");
        devLog("done deleting objects");
        Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
    }

    public void fixMap() {
        Toast.makeText(getApplicationContext(), R.string.info_wait, Toast.LENGTH_SHORT).show();
        devLog("attempting to fix the map");
        try {
            LacMapUtil.fixMap(updatedContent);
        } catch (Exception e) {
            devLog(e.toString());
        }
        devLog("done");
        Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
    }

    public void saveMap() {
        devLog("attempting to save the map");
        StringBuilder newContent = new StringBuilder();
        for (String s : updatedContent) {
            if (newContent.length() > 0) {
                newContent.append("\n").append(s);
            } else {
                newContent.append(s);
            }
        }
        try {
            File fileDir = new File(rawPath.replace(mapName.getText().toString()+".txt", ""));
            File mapFile = new File(fileDir, mapName.getText().toString()+".txt");
            FileWriter writer = new FileWriter(mapFile);
            writer.append(newContent.toString());
            writer.flush();
            writer.close();
            Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
            devLog("done saving the map");
            finish();
        } catch (Exception e) {
            devLog(e.toString());
        }
    }

    void openMapTypeView() {
        MapTypeSheet mapTypeSheet = new MapTypeSheet();
        mapTypeSheet.show(getSupportFragmentManager(), "map_type");
    }

    void devLog(String toLog) {
        AppUtil.devLog(toLog, debugText);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        devLog(requestCode+": hasData = "+(data != null));
        if (requestCode == REQUEST_ROLES && data != null) {
            setLine(LINE_ROLES, "Roles List:"+data.getStringExtra("roles"));
        } else {
            devLog("data is null");
        }
    }

    void setListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());
        AppUtil.handleOnPressEvent(mapName);
        AppUtil.handleOnPressEvent(serverNameLinear);

        serverName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setServerName(serverName.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        AppUtil.handleOnPressEvent(mapTypeLinear);
        AppUtil.handleOnPressEvent(mapTypeButton, this::openMapTypeView);
        AppUtil.handleOnPressEvent(optionsLinear);
        AppUtil.handleOnPressEvent(rolesLinear);
        AppUtil.handleOnPressEvent(editRolesButton, this::editRoles);
        AppUtil.handleOnPressEvent(removeAllTdm, () -> removeAllObjects("Team_"));
        AppUtil.handleOnPressEvent(removeAllRace, () -> removeAllObjects("Checkpoint_Editor"));
        AppUtil.handleOnPressEvent(fixMapButton, this::fixMap);
        AppUtil.handleOnPressEvent(saveChanges, this::saveMap);
    }

    @Override
    public void onMapTypeChoose(int type) {
        setMapType(type);
    }
}