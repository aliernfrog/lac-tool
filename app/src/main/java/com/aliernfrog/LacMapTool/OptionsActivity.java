package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.hbisoft.pickit.PickiT;

@SuppressLint({"UseSwitchCompatOrMaterialCode", "ClickableViewAccessibility"})
public class OptionsActivity extends AppCompatActivity {
    ImageView home;
    LinearLayout optionsApp;
    Switch autoBackups;
    Switch disableUpdates;
    LinearLayout optionsEx;
    Switch onlineFix;
    Switch lacd;
    Switch legacyPath;
    Switch dev;
    Switch test;
    Button deleteTemp;
    TextView changelog;
    LinearLayout discord_linear;
    Button discord_bbots;
    Button discord_rcs;
    Button github;
    Button app_feedback;

    SharedPreferences update;
    SharedPreferences config;
    SharedPreferences.Editor configEdit;

    PickiT pickiT;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        update = getSharedPreferences("APP_UPDATE", Context.MODE_PRIVATE);
        config = getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE);
        configEdit = config.edit();

        home = findViewById(R.id.options_goback);
        optionsApp = findViewById(R.id.options_app);
        autoBackups = findViewById(R.id.options_autobkup);
        disableUpdates = findViewById(R.id.options_disableupdates);
        onlineFix = findViewById(R.id.options_enableOnlineSolution);
        optionsEx = findViewById(R.id.options_ex);
        lacd = findViewById(R.id.options_toggleLACD);
        legacyPath = findViewById(R.id.options_legacypath);
        dev = findViewById(R.id.options_devtoggle);
        test = findViewById(R.id.options_testtoggle);
        deleteTemp = findViewById(R.id.options_deleteTemp);
        discord_linear = findViewById(R.id.options_dc);
        discord_bbots = findViewById(R.id.options_discord_bbots);
        discord_rcs = findViewById(R.id.options_discord_rcs);
        github = findViewById(R.id.options_github);
        app_feedback = findViewById(R.id.options_app_feedback);
        changelog = findViewById(R.id.options_changelog);
        changelog.setText(update.getString("changelog", null));

        pickiT = new PickiT(getApplicationContext(), null, this);

        checkConfig();
        setListener();
    }

    void checkConfig() {
        if (config.getBoolean("enableAutoBackups", false)) autoBackups.setChecked(true);
        if (config.getBoolean("disableUpdates", false)) disableUpdates.setChecked(true);
        if (config.getBoolean("enableLacd", false)) lacd.setChecked(true);
        if (config.getBoolean("enableLegacyPath", false)) legacyPath.setChecked(true);
        if (config.getBoolean("enableOnlineFix", true)) onlineFix.setChecked(true);
        if (config.getBoolean("enableDebug", false)) dev.setChecked(true);
        if (config.getBoolean("enableTest", false)) test.setChecked(true);
        if (!update.getBoolean("showLegacyMode", false) && !config.getBoolean("hidden-enable", false)) legacyPath.setVisibility(View.GONE);
        if (!config.getBoolean("hidden-enable", false)) {
            test.setVisibility(View.GONE);
        }
    }

    void changeOption(String name, Boolean set) {
        configEdit.putBoolean(name, set);
        configEdit.commit();
    }

    void switchActivity(Class i) {
        Intent intent = new Intent(this.getApplicationContext(), i);
        startActivity(intent);
    }

    void redirectURL(String url) {
        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        startActivity(viewIntent);
    }

    void setListener() {
        home.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    finish();
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });
        optionsApp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });
        autoBackups.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeOption("enableAutoBackups", isChecked);
            }
        });
        disableUpdates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeOption("disableUpdates", isChecked);
            }
        });
        optionsEx.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });
        lacd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeOption("enableLacd", isChecked);
            }
        });
        legacyPath.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeOption("enableLegacyPath", isChecked);
            }
        });
        onlineFix.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeOption("enableOnlineFix", isChecked);
            }
        });
        dev.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeOption("enableDebug", isChecked);
            }
        });
        test.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeOption("enableTest", isChecked);
            }
        });
        deleteTemp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    pickiT.deleteTemporaryFile(getApplicationContext());
                    Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });
        discord_linear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });
        discord_bbots.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    redirectURL("https://blursedbots.glitch.me/discord.html");
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });
        discord_rcs.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    redirectURL("https://discord.gg/ExY9V4T");
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });
        github.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    redirectURL("https://github.com/aliernfrog/lac-tool");
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });
        app_feedback.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    switchActivity(FeedbackActivity.class);
                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });
        changelog.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                }
                AppUtil.handleOnPressEvent(v, event);
                return true;
            }
        });
    }
}