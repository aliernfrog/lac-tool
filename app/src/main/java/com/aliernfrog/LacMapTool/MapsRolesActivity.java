package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;

public class MapsRolesActivity extends AppCompatActivity {
    Toolbar toolbar;
    FloatingActionButton saveButton;
    LinearLayout addRoleLinear;
    TextInputEditText addRoleNameInput;
    TextInputEditText addRoleColorInput;
    TextView rawRoleName;
    Button addRoleButton;
    LinearLayout rolesRoot;
    TextView debugText;

    SharedPreferences config;

    ArrayList<String> roleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_roles);

        config = getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE);

        toolbar = findViewById(R.id.mapsRoles_toolbar);
        saveButton = findViewById(R.id.mapsRoles_save);
        addRoleLinear = findViewById(R.id.mapsRoles_addRole_linear);
        addRoleNameInput = findViewById(R.id.mapsRoles_addRole_nameInput);
        addRoleColorInput = findViewById(R.id.mapsRoles_addRole_colorInput);
        rawRoleName = findViewById(R.id.mapsRoles_addRole_rawText);
        addRoleButton = findViewById(R.id.mapsRoles_addRole_done);
        rolesRoot = findViewById(R.id.mapsRoles_root);
        debugText = findViewById(R.id.mapsRoles_debug);

        String rolesString = getIntent().getStringExtra("roles");
        if (rolesString == null) finish();
        if (rolesString != null) roleList = new ArrayList<>(Arrays.asList(rolesString.split(",")));

        if (config.getBoolean("enableDebug", false)) debugText.setVisibility(View.VISIBLE);

        devLog("MapsRolesActivity started");
        devLog("rolesString: "+rolesString);

        readRoles();
        setListeners();
    }

    public void readRoles() {
        devLog("attempting to read roles");
        rolesRoot.removeAllViews();
        for (int i = 0; i < roleList.size(); i++) {
            String role = roleList.get(i);
            addRoleView(role);
        }
    }

    void addRoleView(String role) {
        ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.inflate_role, rolesRoot, false);
        TextView roleName = viewGroup.findViewById(R.id.role_name);
        Button delete = viewGroup.findViewById(R.id.role_delete);
        roleName.setText(Html.fromHtml(roleToHtml(role)));
        roleName.setOnLongClickListener(v -> {
            copyRoleToClipboard(role);
            return true;
        });
        AppUtil.handleOnPressEvent(delete, () -> deleteRole(viewGroup));
        AppUtil.handleOnPressEvent(viewGroup);
        rolesRoot.addView(viewGroup);
    }

    public void copyRoleToClipboard(String role) {
        AppUtil.copyToClipboard(role, getApplicationContext());
        Toast.makeText(getApplicationContext(), getString(R.string.mapRoles_copied)+": "+role, Toast.LENGTH_SHORT).show();
    }

    public void deleteRole(View view) {
        int roleIndex = rolesRoot.indexOfChild(view);
        devLog("attempting to delete role "+roleIndex);
        roleList.remove(roleIndex);
        rolesRoot.removeView(view);
        Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
    }

    public void addRole() {
        String role = getRawRole();
        if (role.equals("") || role.equals("[]")) {
            Toast.makeText(getApplicationContext(), R.string.mapRoles_add_noName, Toast.LENGTH_SHORT).show();
        } else if (role.contains(",") || role.contains(":")) {
            Toast.makeText(getApplicationContext(), R.string.mapRoles_add_cantInclude, Toast.LENGTH_SHORT).show();
        } else {
            devLog("attempting to add role: "+role);
            roleList.add(role);
            addRoleView(role);
            Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
        }
    }

    String getRawRole() {
        String raw = "";
        if (addRoleNameInput.getText() != null) {
            raw = addRoleNameInput.getText().toString();
            if (!raw.equals("")) {
                if (!raw.contains("[")) raw = "["+raw;
                if (!raw.contains("]")) raw = raw+"]";
                if (addRoleColorInput.getText() != null && !addRoleColorInput.getText().toString().equals("")) {
                    String color = addRoleColorInput.getText().toString();
                    raw = "<color="+color+">"+raw+"</color>";
                }
            }
        }
        rawRoleName.setText(raw);
        return raw;
    }

    void saveRolesAndExit() {
        String rolesString = TextUtils.join(",", roleList)+",";
        Intent intent = new Intent();
        intent.putExtra("roles", rolesString);
        setResult(RESULT_OK, intent);
        finish();
    }

    String roleToHtml(String role) {
        return role.replace("<color", "<font color").replace("</color>", "</font>");
    }

    void devLog(String toLog) {
        AppUtil.devLog(toLog, debugText);
    }

    void setListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());
        AppUtil.handleOnPressEvent(saveButton, this::saveRolesAndExit);
        AppUtil.handleOnPressEvent(addRoleLinear);
        addRoleNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                getRawRole();
            }
        });
        addRoleColorInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                getRawRole();
            }
        });
        AppUtil.handleOnPressEvent(addRoleButton, this::addRole);
    }
}