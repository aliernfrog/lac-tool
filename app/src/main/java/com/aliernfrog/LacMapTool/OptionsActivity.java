package com.aliernfrog.LacMapTool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliernfrog.LacMapTool.utils.AppUtil;
import com.aliernfrog.LacMapTool.utils.FileUtil;
import com.aliernfrog.LacMapTool.utils.WebUtil;
import com.hbisoft.pickit.PickiT;

import org.json.JSONObject;

import java.io.File;

@SuppressLint({"UseSwitchCompatOrMaterialCode", "ClickableViewAccessibility"})
public class OptionsActivity extends AppCompatActivity {
    ImageView home;
    LinearLayout lacOptions;
    LinearLayout lacOptionsContent;
    CheckBox lacd;
    CheckBox lacm;
    CheckBox lacmb;
    LinearLayout backupOptions;
    LinearLayout backupOptionsContent;
    CheckBox autoBackups;
    CheckBox backupOnEdit;
    LinearLayout appOptions;
    LinearLayout appOptionsContent;
    CheckBox autoCheckUpdate;
    CheckBox forceEnglish;
    CheckBox dev;
    EditText forceActivity;
    Button deleteTemp;
    LinearLayout discord_linear;
    Button discord_aliern;
    Button discord_rcs;
    Button github;
    LinearLayout feedbackLinear;
    LinearLayout feedback;
    EditText feedbackInput;
    Button feedbackSubmit;
    TextView changelog;

    SharedPreferences update;
    SharedPreferences config;
    SharedPreferences.Editor configEdit;

    PickiT pickiT;

    String tempPath;
    String feedbackUrl = "https://ensibot-discord.aliernfrog.repl.co";

    String appVers;
    Integer appVersCode;

    Integer appOptionsClicks = 0;
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
        lacOptions = findViewById(R.id.options_lac);
        lacOptionsContent = findViewById(R.id.options_lac_content);
        lacd = findViewById(R.id.options_toggleLACD);
        lacm = findViewById(R.id.options_toggleLACM);
        lacmb = findViewById(R.id.options_toggleLACMB);
        backupOptions = findViewById(R.id.options_backup);
        backupOptionsContent = findViewById(R.id.options_backup_content);
        autoBackups = findViewById(R.id.options_autoBackup);
        backupOnEdit = findViewById(R.id.options_backupOnEdit);
        appOptions = findViewById(R.id.options_app);
        appOptionsContent = findViewById(R.id.options_app_content);
        autoCheckUpdate = findViewById(R.id.options_autoCheckUpdate);
        forceEnglish = findViewById(R.id.options_forceEnglish);
        dev = findViewById(R.id.options_devtoggle);
        forceActivity = findViewById(R.id.options_startActivity);
        deleteTemp = findViewById(R.id.options_deleteTemp);
        discord_linear = findViewById(R.id.options_dc);
        discord_aliern = findViewById(R.id.options_discord_aliern);
        discord_rcs = findViewById(R.id.options_discord_rcs);
        github = findViewById(R.id.options_github);
        feedbackLinear = findViewById(R.id.options_feedback_linear);
        feedback = findViewById(R.id.options_feedback);
        feedbackInput = findViewById(R.id.options_feedback_input);
        feedbackSubmit = findViewById(R.id.options_feedback_submit);
        changelog = findViewById(R.id.options_changelog);

        tempPath = update.getString("path-app", null)+"temp";

        pickiT = new PickiT(getApplicationContext(), null, this);

        getVersion();
        getChangelog();
        checkConfig();
        setListener();
    }

    void getVersion() {
        try {
            appVers = AppUtil.getVersName(getApplicationContext());
            appVersCode = AppUtil.getVersCode(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void getChangelog() {
        boolean hasChangelog = update.getString("updateChangelog", null) != null;
        String versInfo = "<b>"+getString(R.string.optionsChangelogCurrent)+":</b> "+appVers+" ("+appVersCode+")";
        String _full;
        if (hasChangelog) {
            String _changelog = update.getString("updateChangelog", null).replaceAll("%VERS%", appVers);
            String _changelogVers = update.getString("updateChangelogVersion", null);
            _full = _changelog+"<br /><br />"+"<b>"+getString(R.string.optionsChangelogChangelog)+":</b> "+_changelogVers+"<br />"+versInfo;
        } else {
            String _noChangelogInfo = getString(R.string.optionsChangelogNoChangelog);
            String _appInfo = "LAC Tool is made by aliernfrog#9747 and is NOT an official app";
            _full = _noChangelogInfo+"<br /><br />"+_appInfo+"<br /><br />"+versInfo;
        }
        changelog.setText(Html.fromHtml(_full));
    }

    void checkConfig() {
        if (config.getBoolean("enableLacd", false)) lacd.setChecked(true);
        if (config.getBoolean("enableLacm", false)) lacm.setChecked(true);
        if (config.getBoolean("enableLacmb", false)) lacmb.setChecked(true);
        if (config.getBoolean("enableAutoBackups", false)) autoBackups.setChecked(true);
        if (config.getBoolean("enableBackupOnEdit", true)) backupOnEdit.setChecked(true);
        if (config.getBoolean("autoCheckUpdates", true)) autoCheckUpdate.setChecked(true);
        if (config.getBoolean("forceEnglish", false)) forceEnglish.setChecked(true);
        if (config.getBoolean("enableDebug", false)) dev.setChecked(true);
    }

    void changeOption(String name, Boolean set) {
        if (name.equals("enableLacd") || name.equals("enableLacm") || name.equals("enableLacmb") || name.equals("enableDebug") || name.equals("forceEnglish")) activityResult = 1;
        configEdit.putBoolean(name, set);
        configEdit.commit();
    }

    void submitFeedback() {
        String feedback = feedbackInput.getText().toString();
        if (feedback.length() < 5) return;
        try {
            JSONObject object = new JSONObject();
            object.put("type", "feedback");
            object.put("body", feedback);
            object.put("from", "LAC Tool "+appVersCode);
            String response = WebUtil.doPostRequest(feedbackUrl, object);
            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
            feedbackLinear.setVisibility(View.GONE);
        } catch (Exception e) {
            feedbackInput.setText(e.toString());
        }
    }

    void deleteTempData() {
        File tempFile = new File(tempPath);
        FileUtil.deleteDirectory(tempFile); //delete app temp data
        pickiT.deleteTemporaryFile(getApplicationContext()); //delete PickiT temp data
        Toast.makeText(getApplicationContext(), R.string.info_done, Toast.LENGTH_SHORT).show();
    }

    void redirectURL(String url) {
        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        startActivity(viewIntent);
    }

    void startActivityWithName(String name) {
        try {
            Class c = Class.forName(getPackageName()+"."+name);
            Intent intent = new Intent(this, c);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        lacOptions.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                AppUtil.toggleView(lacOptionsContent);
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
        lacd.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableLacd", isChecked));
        lacm.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableLacm", isChecked));
        lacmb.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableLacmb", isChecked));

        backupOptions.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                AppUtil.toggleView(backupOptionsContent);
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
        autoBackups.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableAutoBackups", isChecked));
        backupOnEdit.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableBackupOnEdit", isChecked));

        appOptions.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                AppUtil.toggleView(appOptionsContent);
                appOptionsClicks += 1;
                if (appOptionsClicks >= 10) forceActivity.setVisibility(View.VISIBLE);
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
        autoCheckUpdate.setOnCheckedChangeListener(((buttonView, isChecked) -> changeOption("autoCheckUpdates", isChecked)));
        forceEnglish.setOnCheckedChangeListener(((buttonView, isChecked) -> changeOption("forceEnglish", isChecked)));
        dev.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableDebug", isChecked));
        forceActivity.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                startActivityWithName(forceActivity.getText().toString());
                return true;
            }
            return false;
        });

        deleteTemp.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                deleteTempData();
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
        discord_linear.setOnTouchListener((v, event) -> {
            event.getAction();
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
        discord_aliern.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                redirectURL("https://aliernfrog.glitch.me/discord.html");
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
        discord_rcs.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                redirectURL("https://discord.gg/aQhGqHSc3W");
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

        feedbackLinear.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) AppUtil.toggleView(feedback);
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
        feedbackSubmit.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                submitFeedback();
            }
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        changelog.setOnTouchListener((v, event) -> {
            event.getAction();
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }
}