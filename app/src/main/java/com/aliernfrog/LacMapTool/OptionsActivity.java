package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import com.aliernfrog.LacMapTool.utils.FileUtil;
import com.hbisoft.pickit.PickiT;

import java.io.File;

@SuppressLint({"UseSwitchCompatOrMaterialCode", "ClickableViewAccessibility"})
public class OptionsActivity extends AppCompatActivity {
    ImageView home;
    LinearLayout optionsApp;
    Switch autoBackups;
    Switch lacd;
    Switch lacm;
    Switch legacyPath;
    LinearLayout optionsEx;
    Switch disableUpdates;
    Switch onlineFix;
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

    Integer activityResult = 0;

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
        lacd = findViewById(R.id.options_toggleLACD);
        lacm = findViewById(R.id.options_toggleLACM);
        legacyPath = findViewById(R.id.options_legacypath);
        optionsEx = findViewById(R.id.options_ex);
        disableUpdates = findViewById(R.id.options_disableupdates);
        onlineFix = findViewById(R.id.options_enableOnlineSolution);
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
        if (config.getBoolean("enableLacd", false)) lacd.setChecked(true);
        if (config.getBoolean("enableLacm", false)) lacm.setChecked(true);
        if (config.getBoolean("enableLegacyPath", false)) legacyPath.setChecked(true);
        if (config.getBoolean("disableUpdates", false)) disableUpdates.setChecked(true);
        if (config.getBoolean("enableOnlineFix", true)) onlineFix.setChecked(true);
        if (config.getBoolean("enableDebug", false)) dev.setChecked(true);
        if (config.getBoolean("enableTest", false)) test.setChecked(true);
        if (!update.getBoolean("showLegacyMode", false) && !config.getBoolean("hidden-enable", false)) legacyPath.setVisibility(View.GONE);
        if (!config.getBoolean("hidden-enable", false)) {
            test.setVisibility(View.GONE);
        }
    }

    void changeOption(String name, Boolean set) {
        if (name == "enableDebug" || name == "enableTest") activityResult = 1;
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

    void finishActivity() {
        setResult(activityResult);
        finish();
    }

    void setListener() {
        home.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                finishActivity();
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
        optionsApp.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {

            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
        autoBackups.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableAutoBackups", isChecked));
        lacd.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableLacd", isChecked));
        lacm.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableLacm", isChecked));
        legacyPath.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableLegacyPath", isChecked));
        optionsEx.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {

            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
        disableUpdates.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("disableUpdates", isChecked));
        onlineFix.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableOnlineFix", isChecked));
        dev.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableDebug", isChecked));
        test.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableTest", isChecked));
        deleteTemp.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                pickiT.deleteTemporaryFile(getApplicationContext());
                File tempFile = new File(update.getString("path-app", null)+"temp");
                FileUtil.deleteDirectory(tempFile);
                Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
        discord_linear.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {

            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
        discord_bbots.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                redirectURL("https://blursedbots.glitch.me/discord.html");
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
        discord_rcs.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                redirectURL("https://discord.gg/ExY9V4T");
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
        github.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                redirectURL("https://github.com/aliernfrog/lac-tool");
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
        app_feedback.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                switchActivity(FeedbackActivity.class);
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
        changelog.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {

            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }
}