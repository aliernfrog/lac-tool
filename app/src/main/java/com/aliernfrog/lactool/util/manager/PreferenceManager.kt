package com.aliernfrog.lactool.util.manager

import android.content.Context
import android.os.Environment
import com.aliernfrog.lactool.enum.ListSorting
import com.aliernfrog.lactool.enum.ListStyle
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
    val mapsListSorting = intPreference("mapsListSorting", ListSorting.ALPHABETICAL.ordinal)
    val mapsListSortingReversed = booleanPreference("mapsListSortingReversed", false)
    val mapsListStyle = intPreference("mapsListStyle", ListStyle.LIST.ordinal)

    // Maps materials list
    val mapsMaterialsListStyle = intPreference("mapsMaterialsListStyle", ListStyle.GRID.ordinal)

    // Wallpapers list
    val wallpapersListSorting = intPreference("wallpapersListSorting", ListSorting.DATE.ordinal)
    val wallpapersListSortingReversed = booleanPreference("wallpapersListSortingReversed", false)
    val wallpapersListStyle = intPreference("wallpapersListStyle", ListStyle.GRID.ordinal)

    // Screenshots list
    val screenshotsListSorting = intPreference("screenshotsListSorting", ListSorting.DATE.ordinal)
    val screenshotsListSortingReversed = booleanPreference("screenshotsListSortingReversed", false)
    val screenshotsListStyle = intPreference("screenshotsListStyle", ListStyle.GRID.ordinal)

    // Other options
    val showMapNameFieldGuide = booleanPreference("showMapNameFieldGuide", true, experimental = true, includeInDebugInfo = false)
    val showMediaViewGuide = booleanPreference("showMediaViewGuide", true, experimental = true, includeInDebugInfo = false)

    // Experimental (developer) options
    val experimentalOptionsEnabled = booleanPreference("experimentalOptionsEnabled", false)
    val debug = booleanPreference("debug", false, experimental = true, includeInDebugInfo = false)
    val shizukuNeverLoad = booleanPreference("shizukuNeverLoad", false, experimental = true, includeInDebugInfo = false)
    val lastKnownInstalledVersion = longPreference("lastKnownInstalledVersion", GeneralUtil.getAppVersionCode(context), experimental = true, includeInDebugInfo = false)
    val updatesURL = stringPreference("updatesUrl", "https://aliernfrog.github.io/lactool/latest.json", experimental = true, includeInDebugInfo = false)
}