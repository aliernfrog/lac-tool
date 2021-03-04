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
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.aliernfrog.LacMapTool.utils.BackgroundTask;
import com.aliernfrog.LacMapTool.utils.FileUtil;
import com.aliernfrog.LacMapTool.utils.WebUtil;

import org.json.JSONObject;

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
    EditText maxVeh;
    EditText fuelConsume;
    EditText idleVeh;
    Switch healthRegeneration;
    Switch hideNames;
    Switch allowRespawn;
    Switch voiceChat;
    Switch voteRole;
    Switch rolePlay;
    LinearLayout rolesLinear;
    TextView roles_title;
    LinearLayout rolesAdd_linear;
    LinearLayout rolesAdd_linear2;
    EditText rolesAdd_input;
    Button rolesAdd_button;
    TextView rolesAdd_desc;
    LinearLayout optionsLinear;
    Button fixMapButton;
    Button saveChanges;
    TextView devLog;

    SharedPreferences config;

    String postUrl = "https://ensibot-discord.aliernfrog.repl.co";
    Boolean devMode;
    Boolean onlineFix;
    String rawPath;
    Integer mapVers;

    String[] updatedContent;
    String logs = "";

    ArrayList<String> roles = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_options);

        config = getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE);
        rawPath = config.getString("lastPath", null);
        devMode = config.getBoolean("enableDebug", false);
        onlineFix = config.getBoolean("enableOnlineFix", true);

        goback = findViewById(R.id.mapsOptions_goback);
        mapName = findViewById(R.id.mapsOptions_mapName);
        serverName = findViewById(R.id.mapsOptions_serverName_input);
        serverNameLinear = findViewById(R.id.mapsOptions_serverName_linear);
        mapTypeLinear = findViewById(R.id.mapsOptions_mapType_linear);
        mapType = findViewById(R.id.mapsOptions_mapType_spinner);
        maxVeh = findViewById(R.id.mapsOptions_maxveh_input);
        fuelConsume = findViewById(R.id.mapsOptions_fuelconsume_input);
        idleVeh = findViewById(R.id.mapsOptions_idleveh_input);
        healthRegeneration = findViewById(R.id.mapsOptions_healthRegeneration_switch);
        hideNames = findViewById(R.id.mapsOptions_hideNames_switch);
        allowRespawn = findViewById(R.id.mapsOptions_allowRespawn_switch);
        voiceChat = findViewById(R.id.mapsOptions_voiceChat_switch);
        voteRole = findViewById(R.id.mapsOptions_voteRole_switch);
        rolePlay = findViewById(R.id.mapsOptions_rolePlay_switch);
        rolesLinear = findViewById(R.id.mapsOptions_roles_linear);
        roles_title = findViewById(R.id.mapsOptions_roles_title);
        rolesAdd_linear = findViewById(R.id.mapsOptions_roleAdd_linear);
        rolesAdd_linear2 = findViewById(R.id.mapsOptions_roleAdd_linear2);
        rolesAdd_input = findViewById(R.id.mapsOptions_roleAdd_name);
        rolesAdd_button = findViewById(R.id.mapsOptions_roleAdd_button);
        rolesAdd_desc = findViewById(R.id.mapsOptions_roleAdd_desc);
        optionsLinear = findViewById(R.id.mapsOptions_options_linear);
        fixMapButton = findViewById(R.id.mapsOptions_fix_button);
        saveChanges = findViewById(R.id.mapsOptions_save_button);
        devLog = findViewById(R.id.mapsOptions_log);

        rolesAdd_desc.setText(Html.fromHtml("Roles without <font color=yellow>[ ]</font> will get removed by LAC automatically. So always add roles like <font color=yellow>[YOUR ROLE]</font>. To add a role with color do it like <font color=yellow>&#60;color=red&#62;[CRIMINAL]&#60;/color&#62;</font>"));

        putMapTypes();
        getMap(rawPath);
        setListeners();
    }

    public void getMap(String path) {
        rawPath = path.replace("/document/primary:", Environment.getExternalStorageDirectory().toString()+"/").replace("/document/raw:/", "");
        String[] arr = path.replace(".txt", "").split("/");
        mapName.setText(arr[arr.length - 1]);
        devLog("rawPath: "+ rawPath, false);
        devLog("mapName: "+ mapName.getText(), false);
        readMap(path);
    }

    public void readMapProperties() {
        getMapType();
        if (mapVers >= 3) {
            serverName.setText(updatedContent[0].replace("Map Name:",""));
            maxVeh.setText(updatedContent[4].replace("Max Vehicle Count: ", ""));
            fuelConsume.setText(updatedContent[5].replace("Fuel Consume Rate: ", ""));
            idleVeh.setText(updatedContent[6].replace("Delete Idle Vehicle: ", ""));
            healthRegeneration.setChecked(getBoolean(updatedContent[7].replace("Health Regeneration: ", "")));
            hideNames.setChecked(getBoolean(updatedContent[8].replace("Hide Names: ", "")));
            allowRespawn.setChecked(getBoolean(updatedContent[9].replace("Allow Respawn: ", "")));
            voiceChat.setChecked(getBoolean(updatedContent[10].replace("Voice-Chat: ", "")));
            voteRole.setChecked(getBoolean(updatedContent[11].replace("Vote For Role: ", "")));
            rolePlay.setChecked(getBoolean(updatedContent[12].replace("Role-play: ", "")));
        }
        readRoles(true);
        refreshVisibility();
    }

    public void getMapType() {
        String str = updatedContent[1].replace("Map Type:", "");
        if (mapVers < 3) str = updatedContent[0].replace("Map Type:", "");
        Integer typeInt = Integer.parseInt(str);
        mapType.setSelection(typeInt);
    }

    public void setMapName(String name) {
        devLog("attempting to change map name to: "+name, false);
        updatedContent[0] = "Map Name:"+name;
    }

    public void setMapType(Integer mapTypeNumber) {
        devLog("attempting to change map type to: "+mapTypeNumber, false);
        if (mapVers >= 3) {
            updatedContent[1] = "Map Type:"+mapTypeNumber.toString();
        } else {
            updatedContent[0] = "Map Type:"+mapTypeNumber.toString();
        }
    }

    public void setString(Integer posAtContent, String string) {
        devLog("attempting to change line at pos "+posAtContent.toString()+" to: "+string, false);
        updatedContent[posAtContent] = string;
    }

    public void putMapTypes() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner);
        adapter.add(getResources().getString(R.string.mapEdit_type_0));
        adapter.add(getResources().getString(R.string.mapEdit_type_1));
        adapter.add(getResources().getString(R.string.mapEdit_type_2));
        adapter.add(getResources().getString(R.string.mapEdit_type_3));
        adapter.add(getResources().getString(R.string.mapEdit_type_4));
        adapter.add(getResources().getString(R.string.mapEdit_type_5));
        mapType.setAdapter(adapter);
    }

    public void readMap(String path) {
        File mapFile = new File(path);
        if (mapFile.exists()) {
            devLog("attempting to read: "+path, false);
            try {
                String _full = FileUtil.readFile(path);
                updatedContent = _full.split("\n");
                readMapVers();
            } catch (Exception e) {
                devLog(e.toString(), true);
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.denied_doesntExist, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void readMapVers() {
        if (updatedContent[0].contains("Map Name:")) {
            mapVers = 3;
        } else if (updatedContent[3].toLowerCase().contains("camera pos:")) {
            mapVers = 2;
        } else {
            mapVers = 1;
        }
        devLog("mapVers = "+mapVers, false);
        readMapProperties();
    }

    public void refreshVisibility() {
        if (mapVers < 3) {
            serverNameLinear.setVisibility(View.GONE);
            optionsLinear.setVisibility(View.GONE);
        }
    }

    public void readRoles(Boolean putRoles) {
        if (mapVers >= 3) {
            rolesLinear.removeAllViews();
            rolesLinear.addView(roles_title);
            rolesLinear.addView(rolesAdd_linear);
            devLog("attempting to read roles", false);
            rolesLinear.setVisibility(View.VISIBLE);
            String[] rolesString = updatedContent[13].replace("Roles List:", "").split(",");
            if (putRoles) {
                for (int i = 0; i < rolesString.length; i++) {
                    if (!rolesString[i].equals("")) roles.add(rolesString[i]);
                }
            }
            for (int i = 0; i < roles.size(); i++) {
                if (!roles.get(i).equals("")) {
                    String name = roles.get(i);
                    ViewGroup layout = (ViewGroup) getLayoutInflater().inflate(R.layout.role, rolesLinear, false);
                    LinearLayout roleLinear = (LinearLayout) layout.findViewById(R.id.role_bg);
                    TextView roleName = (TextView) layout.findViewById(R.id.role_name);
                    Button roleDel = (Button) layout.findViewById(R.id.role_delete);
                    roleName.setText(name);
                    int finalI = i;
                    roleLinear.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_UP) {

                            }
                            AppUtil.handleOnPressEvent(v, event);
                            return true;
                        }
                    });
                    roleDel.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                roles.remove(finalI);
                                readRoles(false);
                            }
                            AppUtil.handleOnPressEvent(v, event);
                            return true;
                        }
                    });
                    rolesLinear.addView(layout);
                    rolesLinear.removeView(rolesAdd_linear);
                    rolesLinear.addView(rolesAdd_linear);
                }
            }
        }
    }

    public void addRole() {
        String roleName = rolesAdd_input.getText().toString();
        roles.add(roleName);
        devLog("added role: " + roleName, false);
        readRoles(false);
    }

    public void saveRoles() {
        if (mapVers >= 3) {
            devLog("attempting to save roles", false);
            String finalRoles = "";
            for (int i = 0; i < roles.size(); i++) {
                finalRoles += ","+roles.get(i);
            }
            updatedContent[13] = "Roles List:"+finalRoles;
        }
    }

    public void fixMap() {
        Toast.makeText(getApplicationContext(), R.string.info_wait, Toast.LENGTH_SHORT).show();
        if (onlineFix) {
            try {
                JSONObject object = new JSONObject();
                object.put("type", "fixMap");
                object.put("body", arrayJoin(updatedContent, "\n"));
                doPostRequest(postUrl, object);
            } catch (Exception e) {
                devLog(e.toString(), true);
                devLog("attempting to do offline fix", false);
                offlineFix();
            }
        } else {
            devLog("online fix disabled, attempting to do offline fix", false);
            offlineFix();
        }
    }

    public void offlineFix() {
        Boolean is155 = mapVers == 2;

        for (int i = 0; i < updatedContent.length; i++) {
            String line = updatedContent[i];
            String[] strArr = line.split(":");
            ArrayList<String> arr = new ArrayList<String>();
            for (int a = 0; a < strArr.length; a++) {
                arr.add(strArr[a]);
            }
            if (!is155 && line.contains("Editor")) {
                switch (arr.get(0)) {
                    case "Trigger_Box_Editor":
                        arr.add("3.0,3.0,3.0");
                        break;
                    case "Panorama_Object_Editor":
                        arr.add("50.0,50.0,50.0");
                        break;
                    case "Container_Open_Editor":
                        arr.add("1.1,1.1,1.0");
                        break;
                    case "Metal_Railing_Editor":
                        arr.add("0.4,0.4,0.4");
                        break;
                    case "Platform_Green_Editor":
                        arr.add("50.0,1.0,50.0");
                        break;
                    case "Road_Guard_Editor":
                        arr.add("1.2,1.2,1.2");
                        break;
                    case "Soccer_Ball_Editor":
                        arr.add("100.0,100.0,100.0");
                        break;
                    case "Soccer_Map_Editor":
                        arr.add("1.3,1.3,1.3");
                        break;
                    case "Square_Plank_Editor":
                        arr.add("0.7,0.7,0.7");
                        break;
                    case "Stair_Editor":
                        arr.add("1.2,1.2,1.2");
                        break;
                    case "StreetLight_Editor":
                        arr.add("100.1,100.1,100.1");
                        break;
                    case "Tree_Desert_Editor":
                        arr.add("0.5,0.5,0.5");
                        break;
                    case "Tree_Spruce_Editor":
                        arr.add("0.5,0.5,0.5");
                        break;
                    case "Tube_Racing_Curved_Editor":
                        arr.add("2.0,2.0,2.0");
                        break;
                    case "Tube_Racing_Editor":
                        arr.add("2.0,2.0,2.0");
                        break;
                    default:
                        arr.add("1.0,1.0,1.0");
                }
            }
            if (line.contains("Editor")) {
                if (line.contains("Block_1by1_Editor")) {
                    arr.set(0, "Block_Scalable_Editor");
                }
                if (line.contains("Block_3by6_Editor")) {
                    arr.set(0, "Block_Scalable_Editor");
                    arr.set(3, "3.0,6.0,1.0");
                }
                if (line.contains("Sofa_Chunk_Red_Editor")) {
                    arr.set(0, "Sofa_Chunk_Editor");
                    if (!is155) arr.add("color{1.00,0.00,0.00}");
                }
                String full = "";
                for (int a = 0; a < arr.size(); a++) {
                    String str = arr.get(a);
                    full = full+str+":";
                }
                updatedContent[i] = full;
            }
        }
        devLog("done", false);
        Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
    }

    public void saveMap() {
        saveRoles();
        devLog("attempting to save the map", false);
        String newContent = "";
        for (int i = 0; i < updatedContent.length; i++) {
            if (newContent.length() > 0) {
                newContent += "\n"+updatedContent[i];
            } else {
                newContent += updatedContent[i];
            }
        }
        try {
            File fileDir = new File(rawPath.replace(mapName.getText().toString()+".txt", ""));
            File mapFile = new File(fileDir, mapName.getText().toString()+".txt");
            FileWriter writer = new FileWriter(mapFile);
            writer.append(newContent);
            writer.flush();
            writer.close();
            Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
            devLog("done saving the map", false);
            finish();
        } catch (Exception e) {
            devLog(e.toString(), true);
        }
    }

    Boolean getBoolean(String boolString) {
        if (boolString.contains("true") || boolString.contains("enabled")) {
            return true;
        } else {
            return false;
        }
    }

    public void handlePostRes(String res) {
        devLog("got response: "+res, false);
        if (res != null) {
            Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
            updatedContent = res.split("<br />");
        } else {
            devLog("response is null! attempting to do offline fix", false);
            offlineFix();
        }
    }

    public void doPostRequest(String url, JSONObject obj) {
        devLog("attempting to do post request to "+url, false);
        String[] str = {null};
        new BackgroundTask(this) {

            @Override
            public void doInBackground() {
                try {
                    String res = WebUtil.doPostRequest(url, obj);
                    str[0] = res;
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            devLog(e.toString(), true);
                        }
                    });
                }
            }

            @Override
            public void onPostExecute() {
                handlePostRes(str[0]);
            }
        }.execute();
    }

    public String arrayJoin(String[] arr, String chr) {
        String full = "";
        for (int i = 0; i < arr.length; i++) {
            if (i != 0) {
                full = full+chr+arr[i];
            } else {
                full = full+arr[i];
            }
        }
        return full;
    }

    void devLog(String toLog, Boolean error) {
        if (devMode) {
            String tag = Thread.currentThread().getStackTrace()[3].getMethodName();
            if (error) toLog = "<font color=red>"+toLog+"</font>";
            logs = logs+"<br /><font color=#00FFFF>["+tag+"]</font> "+toLog;
            devLog.setText(Html.fromHtml(logs));
        }
    }

    void setListeners() {
        goback.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    finish();
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        mapName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        serverNameLinear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
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

        mapTypeLinear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        mapType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setMapType(position);
                devLog("selected position: "+position, false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        optionsLinear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        maxVeh.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setString(4, "Max Vehicle Count: "+maxVeh.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        fuelConsume.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setString(5, "Fuel Consume Rate: "+fuelConsume.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        idleVeh.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setString(6, "Delete Idle Vehicle: "+idleVeh.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        healthRegeneration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setString(7, "Health Regeneration: "+isChecked);
            }
        });

        hideNames.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setString(8, "Hide Names: "+isChecked);
            }
        });

        allowRespawn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setString(9, "Allow Respawn: "+isChecked);
            }
        });

        voiceChat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setString(10, "Voice-Chat: enabled");
                } else {
                    setString(10, "Voice-Chat: disabled");
                }
            }
        });

        voteRole.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setString(11, "Vote For Role: enabled");
                } else {
                    setString(11, "Vote For Role: disabled");
                }
            }
        });

        rolePlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setString(12, "Role-play: enabled");
                } else {
                    setString(12, "Role-play: disabled");
                }
            }
        });

        rolesLinear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        rolesAdd_linear2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        rolesAdd_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    addRole();
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        rolesAdd_desc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    rolesAdd_desc.setVisibility(View.GONE);
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        fixMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fixMap();
            }
        });

        fixMapButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    fixMap();
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });

        saveChanges.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    saveMap();
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });
    }
}