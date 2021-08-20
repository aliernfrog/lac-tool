package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.aliernfrog.LacMapTool.utils.FileUtil;
import com.aliernfrog.LacMapTool.utils.LacMapUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

@SuppressLint({"ClickableViewAccessibility", "UseSwitchCompatOrMaterialCode"})
public class MapsOptionsActivity extends AppCompatActivity {
    ImageView goback;
    TextView mapName;
    EditText serverName;
    LinearLayout serverNameLinear;
    LinearLayout mapTypeLinear;
    Spinner mapType;
    LinearLayout optionsLinear;
    LinearLayout rolesLinear;
    TextView roles_title;
    LinearLayout rolesAdd_linear;
    LinearLayout rolesAdd_linear2;
    EditText rolesAdd_input;
    Button rolesAdd_button;
    TextView rolesAdd_desc;
    Button removeAllTdm;
    Button removeAllRace;
    Button fixMapButton;
    FloatingActionButton saveChanges;
    TextView debugText;

    SharedPreferences config;

    String rawPath;

    String[] updatedContent;

    Integer LINE_SERVER_NAME;
    Integer LINE_MAP_TYPE;
    Integer LINE_ROLES;
    ArrayList<String> roles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_options);

        config = getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE);
        rawPath = getIntent().getStringExtra("path");
        if (rawPath == null) finish();

        goback = findViewById(R.id.mapsOptions_goback);
        mapName = findViewById(R.id.mapsOptions_mapName);
        serverName = findViewById(R.id.mapsOptions_serverName_input);
        serverNameLinear = findViewById(R.id.mapsOptions_serverName_linear);
        mapTypeLinear = findViewById(R.id.mapsOptions_mapType_linear);
        mapType = findViewById(R.id.mapsOptions_mapType_spinner);
        optionsLinear = findViewById(R.id.mapsOptions_options_linear);
        rolesLinear = findViewById(R.id.mapsOptions_roles_linear);
        roles_title = findViewById(R.id.mapsOptions_roles_title);
        rolesAdd_linear = findViewById(R.id.mapsOptions_roleAdd_linear);
        rolesAdd_linear2 = findViewById(R.id.mapsOptions_roleAdd_linear2);
        rolesAdd_input = findViewById(R.id.mapsOptions_roleAdd_name);
        rolesAdd_button = findViewById(R.id.mapsOptions_roleAdd_button);
        rolesAdd_desc = findViewById(R.id.mapsOptions_roleAdd_desc);
        removeAllTdm = findViewById(R.id.mapsOptions_removeAll_tdm);
        removeAllRace = findViewById(R.id.mapsOptions_removeAll_race);
        fixMapButton = findViewById(R.id.mapsOptions_fix_button);
        saveChanges = findViewById(R.id.mapsOptions_save_button);
        debugText = findViewById(R.id.mapsOptions_log);

        boolean enableDebugUI = config.getBoolean("enableDebug", false);
        if (enableDebugUI) debugText.setVisibility(View.VISIBLE);

        rolesAdd_desc.setText(Html.fromHtml("Roles without <font color=yellow>[ ]</font> will get removed by LAC automatically. So always add roles like <font color=yellow>[YOUR ROLE]</font>. To add a role with color do it like <font color=yellow>&#60;color=red&#62;[CRIMINAL]&#60;/color&#62;</font>"));

        putMapTypes();
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
                    readRoles(i, true);
                case "option":
                    if (!cur.startsWith("Roles List:")) readOption(i);
            }
        }
    }

    public void readOption(Integer line) {
        String str = updatedContent[line];
        String[] split = str.split(":");
        String title = split[0];
        String value = split[1];
        Boolean isNumber = AppUtil.stringIsNumber(value);
        if (isNumber) {
            addNumberOption(line, title, value);
        } else {
            Boolean bool = value.contains("true") || value.contains("enabled");
            addBoolOption(line, title, bool);
        }
    }

    public void addNumberOption(Integer line, String title, String value) {
        ViewGroup view = (ViewGroup) getLayoutInflater().inflate(R.layout.option_number, optionsLinear, false);
        TextView titleView = view.findViewById(R.id.option_number_title);
        EditText valueView = view.findViewById(R.id.option_number_value);
        titleView.setText(title);
        valueView.setText(value);
        valueView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setString(line, valueView.getText().toString());
            }
        });
        optionsLinear.addView(view);
    }

    public void addBoolOption(Integer line, String title, Boolean value) {
        View view = getLayoutInflater().inflate(R.layout.option_bool, optionsLinear, false);
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
        int typeInt = Integer.parseInt(str);
        mapType.setSelection(typeInt);
        mapTypeLinear.setVisibility(View.VISIBLE);
    }

    public void setMapName(String name) {
        devLog("attempting to change map name to: "+name);
        updatedContent[0] = "Map Name:"+name;
    }

    public void setMapType(Integer mapTypeNumber) {
        devLog("attempting to change map type to: "+mapTypeNumber);
        updatedContent[LINE_MAP_TYPE] = "Map Type:"+mapTypeNumber.toString();
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

    public void readRoles(Integer line, Boolean putRoles) {
        LINE_ROLES = line;
        rolesLinear.setVisibility(View.VISIBLE);
        rolesLinear.removeAllViews();
        rolesLinear.addView(roles_title);
        rolesLinear.addView(rolesAdd_linear);
        devLog("attempting to read roles");
        rolesLinear.setVisibility(View.VISIBLE);
        String[] rolesString = updatedContent[line].replace("Roles List:", "").split(",");
        if (putRoles) {
            for (String s : rolesString) {
                if (!s.equals("")) roles.add(s);
            }
        }
        for (int i = 0; i < roles.size(); i++) {
            if (!roles.get(i).equals("")) {
                String name = roles.get(i);
                ViewGroup layout = (ViewGroup) getLayoutInflater().inflate(R.layout.role, rolesLinear, false);
                LinearLayout roleLinear = layout.findViewById(R.id.role_bg);
                TextView roleName = layout.findViewById(R.id.role_name);
                Button roleDel = layout.findViewById(R.id.role_delete);
                roleName.setText(name);
                int finalI = i;
                roleLinear.setOnTouchListener((v, event) -> {
                    event.getAction();
                    AppUtil.handleOnPressEvent(v, event);
                    return true;
                });
                roleDel.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        roles.remove(finalI);
                        readRoles(line, false);
                    }
                    AppUtil.handleOnPressEvent(v, event);
                    return true;
                });
                rolesLinear.addView(layout);
                rolesLinear.removeView(rolesAdd_linear);
                rolesLinear.addView(rolesAdd_linear);
            }
        }
    }

    public void addRole() {
        String roleName = rolesAdd_input.getText().toString();
        roles.add(roleName);
        devLog("added role: " + roleName);
        readRoles(LINE_ROLES, false);
    }

    public void saveRoles() {
        if (LINE_ROLES == null) return;
        devLog("attempting to save roles");
        StringBuilder finalRoles = new StringBuilder();
        for (int i = 0; i < roles.size(); i++) {
            finalRoles.append(",").append(roles.get(i));
        }
        updatedContent[LINE_ROLES] = "Roles List:"+finalRoles;
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
        updatedContent = LacMapUtil.fixMap(updatedContent);
        devLog("done");
        Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
    }

    public void saveMap() {
        saveRoles();
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

    public void putMapTypes() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner);
        adapter.add(getResources().getString(R.string.mapEdit_type_0));
        adapter.add(getResources().getString(R.string.mapEdit_type_1));
        adapter.add(getResources().getString(R.string.mapEdit_type_2));
        adapter.add(getResources().getString(R.string.mapEdit_type_3));
        adapter.add(getResources().getString(R.string.mapEdit_type_4));
        adapter.add(getResources().getString(R.string.mapEdit_type_5));
        mapType.setAdapter(adapter);
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
        goback.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                finish();
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        mapName.setOnTouchListener((v, event) -> {
            event.getAction();
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        serverNameLinear.setOnTouchListener((v, event) -> {
            event.getAction();
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        serverName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setMapName(serverName.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mapTypeLinear.setOnTouchListener((v, event) -> {
            event.getAction();
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        mapType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setMapType(position);
                devLog("selected position: "+position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        optionsLinear.setOnTouchListener((v, event) -> {
            event.getAction();
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        rolesLinear.setOnTouchListener((v, event) -> {
            event.getAction();
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        rolesAdd_linear2.setOnTouchListener((v, event) -> {
            event.getAction();
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        rolesAdd_button.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                addRole();
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        rolesAdd_desc.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                rolesAdd_desc.setVisibility(View.GONE);
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        removeAllTdm.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                removeAllObjects("Team_");
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        removeAllRace.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                removeAllObjects("Checkpoint_Editor");
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        fixMapButton.setOnClickListener(v -> fixMap());

        fixMapButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                fixMap();
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        saveChanges.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                saveMap();
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
    }
}