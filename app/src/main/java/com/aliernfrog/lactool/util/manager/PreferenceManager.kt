package com.aliernfrog.lactool.util.manager

import android.content.Context
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.enum.ListSorting
import com.aliernfrog.lactool.enum.ListStyle
import com.aliernfrog.lactool.enum.StorageAccessType
import com.aliernfrog.lactool.ui.theme.Theme
import com.aliernfrog.lactool.util.manager.base.BasePreferenceManager

class PreferenceManager(context: Context) : BasePreferenceManager(
    prefs = context.getSharedPreferences(ConfigKey.PREF_NAME, Context.MODE_PRIVATE)
) {
    // Appearance options
    val theme = intPreference("appTheme", Theme.SYSTEM.ordinal)
    val materialYou = booleanPreference("materialYou", true)
    val pitchBlack = booleanPreference("pitchBlack", false)

    // General options
    val showChosenMapThumbnail = booleanPreference("chosenMapThumbnail", true)
    val showMapThumbnailsInList = booleanPreference("showMapThumbnailsList", true)
    val language = stringPreference("appLanguage", "") // follow system if blank
    val autoCheckUpdates = booleanPreference("autoUpdates", true)

    // Directory options
    val lacMapsDir = stringPreference("mapsDir", ConfigKey.RECOMMENDED_MAPS_DIR)
    val lacWallpapersDir = stringPreference("wallpapersDir", ConfigKey.RECOMMENDED_WALLPAPERS_DIR)
    val lacScreenshotsDir = stringPreference("screenshotsDir", ConfigKey.RECOMMENDED_SCREENSHOTS_DIR)
    val exportedMapsDir = stringPreference("mapsExportDir", ConfigKey.RECOMMENDED_EXPORTED_MAPS_DIR)
    val storageAccessType = intPreference("storageAccessType", StorageAccessType.SAF.ordinal)

    // Wallpapers list
    val wallpapersListSorting = intPreference("wallpapersListSorting", ListSorting.DATE.ordinal)
    val wallpapersListSortingReversed = booleanPreference("wallpapersListSortingReversed", false)
    val wallpapersListStyle = intPreference("wallpapersListStyle", ListStyle.GRID.ordinal)

    // Screenshots list
    val screenshotsListSorting = intPreference("screenshotsListSorting", ListSorting.DATE.ordinal)
    val screenshotsListSortingReversed = booleanPreference("screenshotsListSortingReversed", false)
    val screenshotsListStyle = intPreference("screenshotsListStyle", ListStyle.GRID.ordinal)

    // Other options
    val showMapNameFieldGuide = booleanPreference("showMapNameFieldGuide", true)
    val showMediaViewGuide = booleanPreference("showMediaViewGuide", true)

    // Experimental (developer) options
    val experimentalOptionsEnabled = booleanPreference("experimentalOptionsEnabled", false)
    val shizukuNeverLoad = booleanPreference("shizukuNeverLoad", false)
    val updatesURL = stringPreference("updatesUrl", ConfigKey.DEFAULT_UPDATES_URL)
}