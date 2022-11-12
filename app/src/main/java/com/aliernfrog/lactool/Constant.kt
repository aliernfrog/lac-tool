package com.aliernfrog.lactool

import android.os.Environment
import com.aliernfrog.lactool.utils.AppUtil

object ConfigKey {
    const val PREF_NAME = "APP_CONFIG"
    const val KEY_APP_THEME = "appTheme"
    const val KEY_APP_AUTO_CHECK_UPDATES = "autoCheckUpdates"
    val DEFAULT_APP_PATH = "${AppUtil.getAppPath()}/LacMapTool"
    val DEFAULT_LAC_PATH = "${Environment.getExternalStorageDirectory()}/Android/data/com.MA.LAC/files"
}

object UpdateKey {
    const val PREF_NAME = "APP_UPDATE"
    const val KEY_APP_VERSION_NAME = "versionName"
    const val KEY_APP_VERSION_CODE = "versionCode"
    const val KEY_PATH_MAPS = "path-maps"
    const val KEY_PATH_WALLPAPERS = "path-wallpapers"
    const val KEY_PATH_SCREENSHOTS = "path-screenshots"
    const val KEY_PATH_LAC = "path-lac"
    const val KEY_PATH_APP = "path-app"
    const val KEY_PATH_TEMP = "path-temp"
    const val KEY_PATH_TEMP_MAPS = "path-temp-maps"
    const val KEY_PATH_TEMP_WALLPAPERS = "path-temp-wallpapers"
    const val KEY_PATH_TEMP_SCREENSHOTS = "path-temp-screenshots"
}