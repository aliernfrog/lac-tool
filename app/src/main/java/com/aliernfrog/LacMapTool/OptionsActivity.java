package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    CheckBox autoBackups;
    CheckBox backupOnEdit;
    CheckBox lacd;
    CheckBox lacm;
    CheckBox legacyPath;
    LinearLayout optionsEx;
    CheckBox dev;
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
        autoBackups = findViewById(R.id.options_autobkup);
        backupOnEdit = findViewById(R.id.options_bkupOnEdit);
        lacd = findViewById(R.id.options_toggleLACD);
        lacm = findViewById(R.id.options_toggleLACM);
        legacyPath = findViewById(R.id.options_legacypath);
        optionsEx = findViewById(R.id.options_ex);
        dev = findViewById(R.id.options_devtoggle);
        deleteTemp = findViewById(R.id.options_deleteTemp);
        discord_linear = findViewById(R.id.options_dc);
        discord_bbots = findViewById(R.id.options_discord_bbots);
        discord_rcs = findViewById(R.id.options_discord_rcs);
        github = findViewById(R.id.options_github);
        app_feedback = findViewById(R.id.options_app_feedback);
        changelog = findViewById(R.id.options_changelog);

        try {
            String _log = update.getString("changelog", null).replaceAll("\n", "<br />");
            String _versName = AppUtil.getVersName(getApplicationContext());
            Integer _versCode = AppUtil.getVersCode(getApplicationContext());
            changelog.setText(Html.fromHtml("<b>Changelog</b><br />"+_log+"<br /><br /><b>Version:</b> "+_versName+" ("+_versCode+")"));
        } catch (Exception e) {
            e.printStackTrace();
            changelog.setText(e.toString());
        }

        pickiT = new PickiT(getApplicationContext(), null, this);

        checkConfig();
        setListener();
    }

    void checkConfig() {
        if (config.getBoolean("enableAutoBackups", false)) autoBackups.setChecked(true);
        if (config.getBoolean("enableBackupOnEdit", true)) backupOnEdit.setChecked(true);
        if (config.getBoolean("enableLacd", false)) lacd.setChecked(true);
        if (config.getBoolean("enableLacm", false)) lacm.setChecked(true);
        if (config.getBoolean("enableLegacyPath", false)) legacyPath.setChecked(true);
        if (config.getBoolean("enableDebug", false)) dev.setChecked(true);
        if (!update.getBoolean("showLegacyMode", false)) legacyPath.setVisibility(View.GONE);
    }

    void changeOption(String name, Boolean set) {
        if (name == "enableLacd" || name == "enableLacm" || name == "enableDebug") activityResult = 1;
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
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        autoBackups.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableAutoBackups", isChecked));
        backupOnEdit.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableBackupOnEdit", isChecked));
        lacd.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableLacd", isChecked));
        lacm.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableLacm", isChecked));
        legacyPath.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableLegacyPath", isChecked));
        dev.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableDebug", isChecked));

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