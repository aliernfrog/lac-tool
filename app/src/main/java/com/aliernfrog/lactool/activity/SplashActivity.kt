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
import com.aliernfrog.lactool.MainActivity
import java.lang.Exception

@SuppressLint("CommitPrefEdits", "CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var prefsUpdate: SharedPreferences
    private lateinit var prefsConfig: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        StrictMode.setThreadPolicy(ThreadPolicy.Builder().permitAll().build())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        prefsUpdate = getSharedPreferences("APP_UPDATE", MODE_PRIVATE)
        prefsConfig = getSharedPreferences("APP_CONFIG", MODE_PRIVATE)

        getVersion()
        setTheme()
        checkUpdatesAndStart()
    }

    private fun getVersion() {
        val updateEditor = prefsUpdate.edit()
        updateEditor.putString("versionName", AppUtil.getVersName(applicationContext))
        updateEditor.putInt("versionCode", AppUtil.getVersCode(applicationContext))
        updateEditor.apply()
    }

    private fun setTheme() {
        val theme = prefsConfig.getInt("appTheme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(theme)
    }

    private fun checkUpdatesAndStart() {
        val shouldCheck = prefsConfig.getBoolean("autoCheckUpdates", true)
        if (shouldCheck) {
            try {
                AppUtil.getUpdates(applicationContext)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        setConfig()
    }

    private fun setConfig() {
        val updateEditor = prefsUpdate.edit()
        val appPath = ConfigKey.DEFAULT_APP_PATH
        val lacPath = ConfigKey.DEFAULT_LAC_PATH
        val tempPath = "$appPath/temp"
        updateEditor.putString("path-maps", "$lacPath/editor")
        updateEditor.putString("path-wallpapers", "$lacPath/wallpaper")
        updateEditor.putString("path-screenshots", "$lacPath/screenshots")
        updateEditor.putString("path-lac", lacPath)
        updateEditor.putString("path-app", appPath)
        updateEditor.putString("path-temp", tempPath)
        updateEditor.putString("path-temp-maps", "$tempPath/editor")
        updateEditor.putString("path-temp-wallpapers", "$tempPath/wallpaper")
        updateEditor.putString("path-temp-screenshots", "$tempPath/screenshots")
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