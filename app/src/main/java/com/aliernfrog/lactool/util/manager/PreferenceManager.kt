package com.aliernfrog.lactool.util.manager

import android.content.Context
import android.os.Environment
import com.aliernfrog.lactool.enum.StorageAccessType
import com.aliernfrog.lactool.externalStorageRoot
import com.aliernfrog.lactool.ui.theme.Theme
import com.aliernfrog.lactool.util.manager.base.BasePreferenceManager
import com.aliernfrog.lactool.util.staticutil.GeneralUtil

class PreferenceManager(context: Context) : BasePreferenceManager(
    prefs = context.getSharedPreferences("APP_CONFIG", Context.MODE_PRIVATE)
) {
    // Appearance options
    val theme = intPreference("appTheme", Theme.SYSTEM.ordinal)
    val materialYou = booleanPreference("materialYou", true)
    val pitchBlack = booleanPreference("pitchBlack", false)

    // General options
    val language = stringPreference("appLanguage", "") // follow system if blank
    val autoCheckUpdates = booleanPreference("autoUpdates", true)

    // Maps options
    val showChosenMapThumbnail = booleanPreference("chosenMapThumbnail", true)
    val showMapThumbnailsInList = booleanPreference("showMapThumbnailsList", true)

    // Directory options
    val lacMapsDir = stringPreference("mapsDir", "${externalStorageRoot}Android/data/com.MA.LAC/files/editor", experimental = true)
    val lacWallpapersDir = stringPreference("wallpapersDir", "${externalStorageRoot}Android/data/com.MA.LAC/files/wallpaper", experimental = true)
    val lacScreenshotsDir = stringPreference("screenshotsDir", "${externalStorageRoot}Android/data/com.MA.LAC/files/screenshots", experimental = true)
    val exportedMapsDir = stringPreference("mapsExportDir", "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/LACTool/exported", experimental = true)
    val storageAccessType = intPreference("storageAccessType", StorageAccessType.SAF.ordinal, includeInDebugInfo = true)

    // Maps list
    val mapsListOptions = listViewOptionsPreference("mapsList")

    // Maps materials list
    val mapsMaterialsListOptions = listViewOptionsPreference("mapsMaterialsList")

    // Wallpapers list
    val wallpapersListOptions = listViewOptionsPreference("wallpapersList")

    // Screenshots list
    val screenshotsListOptions = listViewOptionsPreference("screenshotsList")

    // Other options
    val showMapNameFieldGuide = booleanPreference("showMapNameFieldGuide", true, experimental = true, includeInDebugInfo = false)
    val showMediaViewGuide = booleanPreference("showMediaViewGuide", true, experimental = true, includeInDebugInfo = false)

    // Experimental (developer) options
    val experimentalOptionsEnabled = booleanPreference("experimentalOptionsEnabled", false)
    val ignoreDocumentsUIRestrictions = booleanPreference("ignoreDocumentsUiRestrictions", false, experimental = true, includeInDebugInfo = false)
    val forceStorageAccessTypeCompatibility = booleanPreference("forceStorageAccessTypeCompatibility", false, experimental = true, includeInDebugInfo = false)
    val debug = booleanPreference("debug", false, experimental = true, includeInDebugInfo = false)
    val shizukuNeverLoad = booleanPreference("shizukuNeverLoad", false, experimental = true, includeInDebugInfo = false)
    val lastKnownInstalledVersion = longPreference("lastKnownInstalledVersion", GeneralUtil.getAppVersionCode(context), experimental = true, includeInDebugInfo = false)
    val updatesURL = stringPreference("updatesUrl", "https://aliernfrog.github.io/lactool/latest.json", experimental = true, includeInDebugInfo = false)
}