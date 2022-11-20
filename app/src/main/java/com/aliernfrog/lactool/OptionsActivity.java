package com.aliernfrog.lactool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliernfrog.LacMapTool.R;
import com.aliernfrog.lactool.activity.SplashActivity;
import com.aliernfrog.lactool.fragment.ThemeSheet;
import com.aliernfrog.lactool.utils.AppUtil;
import com.hbisoft.pickit.PickiT;

@SuppressLint({"UseSwitchCompatOrMaterialCode", "ClickableViewAccessibility"})
public class OptionsActivity extends AppCompatActivity {
    Toolbar toolbar;
    LinearLayout backupOptions;
    CheckBox backupOnEdit;
    LinearLayout appOptions;
    CheckBox useInAppFilePicker;
    CheckBox autoCheckUpdate;
    CheckBox dev;
    Button changeTheme;
    Button deleteTemp;
    LinearLayout experimentalOptions;
    EditText startActivityName;
    EditText uriSdkVersionInput;
    EditText updateUrlInput;
    CheckBox forceFdroid;
    LinearLayout changelogLinear;
    TextView changelog;
    LinearLayout social_linear;
    LinearLayout discord_lac;
    LinearLayout github;

    SharedPreferences update;
    SharedPreferences config;
    SharedPreferences.Editor configEdit;

    PickiT pickiT;

    String tempPath;

    String appVers;
    Integer appVersCode;

    Integer changelogClicks = 0;
    Boolean requiresRestart = false;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        update = getSharedPreferences("APP_UPDATE", Context.MODE_PRIVATE);
        config = getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE);
        configEdit = config.edit();

        toolbar = findViewById(R.id.options_toolbar);
        backupOptions = findViewById(R.id.options_backup);
        backupOnEdit = findViewById(R.id.options_backupOnEdit);
        appOptions = findViewById(R.id.options_app);
        useInAppFilePicker = findViewById(R.id.options_useInAppFilePicker);
        autoCheckUpdate = findViewById(R.id.options_autoCheckUpdate);
        dev = findViewById(R.id.options_devtoggle);
        changeTheme = findViewById(R.id.options_changeTheme);
        deleteTemp = findViewById(R.id.options_deleteTemp);
        experimentalOptions = findViewById(R.id.options_ex);
        startActivityName = findViewById(R.id.options_startActivity);
        uriSdkVersionInput = findViewById(R.id.options_uriSdkVersion);
        updateUrlInput = findViewById(R.id.options_updateUrl);
        forceFdroid = findViewById(R.id.options_forceFdroid);
        changelogLinear = findViewById(R.id.options_changelog_linear);
        changelog = findViewById(R.id.options_changelog);
        social_linear = findViewById(R.id.options_social);
        discord_lac = findViewById(R.id.options_social_discordLac);
        github = findViewById(R.id.options_social_githubLacTool);

        tempPath = update.getString("path-temp", null);

        pickiT = new PickiT(getApplicationContext(), null, this);

        getVersion();
        getChangelog();
        checkConfig();
        setListeners();
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
        if (config.getBoolean("useInAppFilePicker", false)) useInAppFilePicker.setChecked(true);
        if (config.getBoolean("enableBackupOnEdit", true)) backupOnEdit.setChecked(true);
        if (config.getBoolean("autoCheckUpdates", true)) autoCheckUpdate.setChecked(true);
        if (config.getBoolean("enableDebug", false)) dev.setChecked(true);
        if (config.getBoolean("forceFdroid", false)) forceFdroid.setChecked(true);
    }

    void changeBoolean(String name, Boolean value) {
        if (name.equals("enableDebug") || name.equals("forceEnglish")) requiresRestart = true;
        configEdit.putBoolean(name, value);
        configEdit.commit();
    }

    void openChangeThemeView() {
        ThemeSheet themeSheet = new ThemeSheet();
        themeSheet.show(getSupportFragmentManager(), "theme_change");
    }

    void deleteTempData() {
        AppUtil.clearTempData(tempPath);
        pickiT.deleteTemporaryFile(getApplicationContext());
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
        finish();
        if (requiresRestart) {
            Intent intent = new Intent(this, SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    void setListeners() {
        toolbar.setNavigationOnClickListener(v -> finishActivity());
        AppUtil.handleOnPressEvent(backupOptions);
        backupOnEdit.setOnCheckedChangeListener((buttonView, isChecked) -> changeBoolean("enableBackupOnEdit", isChecked));
        AppUtil.handleOnPressEvent(appOptions);
        useInAppFilePicker.setOnCheckedChangeListener(((buttonView, isChecked) -> changeBoolean("useInAppFilePicker", isChecked)));
        autoCheckUpdate.setOnCheckedChangeListener(((buttonView, isChecked) -> changeBoolean("autoCheckUpdates", isChecked)));
        dev.setOnCheckedChangeListener((buttonView, isChecked) -> changeBoolean("enableDebug", isChecked));
        AppUtil.handleOnPressEvent(changeTheme, this::openChangeThemeView);
        AppUtil.handleOnPressEvent(deleteTemp, this::deleteTempData);
        AppUtil.handleOnPressEvent(experimentalOptions);

        startActivityName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                startActivityWithName(startActivityName.getText().toString());
                return true;
            }
            return false;
        });
        uriSdkVersionInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                configEdit.putInt("uriSdkVersion", Integer.parseInt(uriSdkVersionInput.getText().toString()));
                configEdit.commit();
                return true;
            }
            return false;
        });
        updateUrlInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                configEdit.putString("updateUrl", updateUrlInput.getText().toString());
                configEdit.commit();
                return true;
            }
            return false;
        });

        forceFdroid.setOnCheckedChangeListener((buttonView, isChecked) -> changeBoolean("forceFdroid", isChecked));
        AppUtil.handleOnPressEvent(changelogLinear, () -> {
            changelogClicks += 1;
            if (changelogClicks > 15) experimentalOptions.setVisibility(View.VISIBLE);
        });
        AppUtil.handleOnPressEvent(social_linear);
        AppUtil.handleOnPressEvent(discord_lac, () -> redirectURL("https://discord.gg/aQhGqHSc3W"));
        AppUtil.handleOnPressEvent(github, () -> redirectURL("https://github.com/aliernfrog/lac-tool"));
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }
}