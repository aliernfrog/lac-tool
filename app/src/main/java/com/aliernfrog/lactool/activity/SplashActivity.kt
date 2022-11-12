package com.aliernfrog.lactool.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences
import android.os.Bundle
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode
import com.aliernfrog.LacMapTool.R
import com.aliernfrog.lactool.utils.AppUtil
import androidx.appcompat.app.AppCompatDelegate
import android.content.Intent
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.UpdateKey
import java.lang.Exception

@SuppressLint("CommitPrefEdits", "CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var prefsUpdate: SharedPreferences
    private lateinit var prefsConfig: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        StrictMode.setThreadPolicy(ThreadPolicy.Builder().permitAll().build())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        prefsUpdate = getSharedPreferences(UpdateKey.PREF_NAME, MODE_PRIVATE)
        prefsConfig = getSharedPreferences(ConfigKey.PREF_NAME, MODE_PRIVATE)

        getVersion()
        setTheme()
        checkUpdatesAndStart()
    }

    private fun getVersion() {
        val updateEditor = prefsUpdate.edit()
        updateEditor.putString(UpdateKey.KEY_APP_VERSION_NAME, AppUtil.getVersName(applicationContext))
        updateEditor.putInt(UpdateKey.KEY_APP_VERSION_CODE, AppUtil.getVersCode(applicationContext))
        updateEditor.apply()
    }

    private fun setTheme() {
        val theme = prefsConfig.getInt(ConfigKey.KEY_APP_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(theme)
    }

    private fun checkUpdatesAndStart() {
        val shouldCheck = prefsConfig.getBoolean(ConfigKey.KEY_APP_AUTO_CHECK_UPDATES, true)
        if (shouldCheck) try {
            AppUtil.getUpdates(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setConfig()
    }

    private fun setConfig() {
        val updateEditor = prefsUpdate.edit()
        val appPath = ConfigKey.DEFAULT_APP_PATH
        val lacPath = ConfigKey.DEFAULT_LAC_PATH
        val tempPath = "$appPath/temp"
        updateEditor.putString(UpdateKey.KEY_PATH_MAPS, "$lacPath/editor")
        updateEditor.putString(UpdateKey.KEY_PATH_WALLPAPERS, "$lacPath/wallpaper")
        updateEditor.putString(UpdateKey.KEY_PATH_SCREENSHOTS, "$lacPath/screenshots")
        updateEditor.putString(UpdateKey.KEY_PATH_LAC, lacPath)
        updateEditor.putString(UpdateKey.KEY_PATH_APP, appPath)
        updateEditor.putString(UpdateKey.KEY_PATH_TEMP, tempPath)
        updateEditor.putString(UpdateKey.KEY_PATH_TEMP_MAPS, "$tempPath/editor")
        updateEditor.putString(UpdateKey.KEY_PATH_TEMP_WALLPAPERS, "$tempPath/wallpaper")
        updateEditor.putString(UpdateKey.KEY_PATH_TEMP_SCREENSHOTS, "$tempPath/screenshots")
        updateEditor.apply()
        clearTempData(tempPath)
    }

    private fun clearTempData(tempPath: String) {
        AppUtil.clearTempData(tempPath)
        switchActivity()
    }

    private fun switchActivity() {
        val intent = Intent(this.applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}