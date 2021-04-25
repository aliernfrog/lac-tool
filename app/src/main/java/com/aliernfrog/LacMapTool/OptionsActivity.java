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
import android.view.MotionEvent;
import android.view.View;
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
    LinearLayout optionsEx;
    CheckBox autoBackups;
    CheckBox backupOnEdit;
    CheckBox lacd;
    CheckBox lacm;
    CheckBox legacyPath;
    CheckBox forceEnglish;
    CheckBox dev;
    Button deleteTemp;
    LinearLayout discord_linear;
    Button discord_bbots;
    Button discord_rcs;
    Button github;
    LinearLayout feedbackLinear;
    LinearLayout feedback;
    EditText feedbackInput;
    Button feedbackSubmit;
    TextView version;

    SharedPreferences update;
    SharedPreferences config;
    SharedPreferences.Editor configEdit;

    PickiT pickiT;

    String tempPath;
    String feedbackUrl = "https://ensibot-discord.aliernfrog.repl.co";

    Integer activityResult = 0; //this will be the result when exiting the activity, if 1 the app will restart

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
        forceEnglish = findViewById(R.id.options_forceEnglish);
        dev = findViewById(R.id.options_devtoggle);
        deleteTemp = findViewById(R.id.options_deleteTemp);
        discord_linear = findViewById(R.id.options_dc);
        discord_bbots = findViewById(R.id.options_discord_bbots);
        discord_rcs = findViewById(R.id.options_discord_rcs);
        github = findViewById(R.id.options_github);
        feedbackLinear = findViewById(R.id.options_feedback_linear);
        feedback = findViewById(R.id.options_feedback);
        feedbackInput = findViewById(R.id.options_feedback_input);
        feedbackSubmit = findViewById(R.id.options_feedback_submit);
        version = findViewById(R.id.options_version);

        tempPath = update.getString("path-app", null)+"temp";

        try {
            String _log = "LAC Tool app was made by aliernfrog#9747 and is NOT an official app";
            String _versName = AppUtil.getVersName(getApplicationContext());
            Integer _versCode = AppUtil.getVersCode(getApplicationContext());
            version.setText(Html.fromHtml(_log+"<br /><br /><b>Version:</b> "+_versName+" ("+_versCode+")"));
        } catch (Exception e) {
            e.printStackTrace();
            version.setText(e.toString());
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
        if (config.getBoolean("forceEnglish", false)) forceEnglish.setChecked(true);
        if (config.getBoolean("enableDebug", false)) dev.setChecked(true);
    }

    void changeOption(String name, Boolean set) {
        if (name.equals("enableLacd") || name.equals("enableLacm") || name.equals("enableDebug") || name.equals("forceEnglish")) activityResult = 1; //set activityResult to 1 so the app will restart on exit
        configEdit.putBoolean(name, set);
        configEdit.commit();
    }

    void submitFeedback() {
        String feedback = feedbackInput.getText().toString();
        if (feedback == null || feedback.length() < 5) return;
        try {
        JSONObject object = new JSONObject();
        object.put("type", "feedback");
        object.put("body", feedback);
        String response = WebUtil.doPostRequest(feedbackUrl, object);
        Boolean success = response != null;
        if (success) {
            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Service is offline", Toast.LENGTH_SHORT).show();
        }
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

    void finishActivity() {
        //sets the result and finishes the activity
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

        optionsEx.setOnTouchListener((v, event) -> {
            AppUtil.handleOnPressEvent(v, event);
            return true;
        });

        autoBackups.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableAutoBackups", isChecked));
        backupOnEdit.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableBackupOnEdit", isChecked));
        lacd.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableLacd", isChecked));
        lacm.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableLacm", isChecked));
        legacyPath.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableLegacyPath", isChecked));
        forceEnglish.setOnCheckedChangeListener(((buttonView, isChecked) -> changeOption("forceEnglish", isChecked)));
        dev.setOnCheckedChangeListener((buttonView, isChecked) -> changeOption("enableDebug", isChecked));

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

        feedbackLinear.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (feedback.getVisibility() != View.VISIBLE) {
                    feedback.setVisibility(View.VISIBLE);
                    feedbackLinear.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.linear));
                }
            }
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

        version.setOnTouchListener((v, event) -> {
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