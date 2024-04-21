package com.aliernfrog.lactool.util.manager

import android.content.Context
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.enum.StorageAccessType
import com.aliernfrog.lactool.ui.theme.Theme
import com.aliernfrog.lactool.util.manager.base.BasePreferenceManager

class PreferenceManager(context: Context) : BasePreferenceManager(
    prefs = context.getSharedPreferences(ConfigKey.PREF_NAME, Context.MODE_PRIVATE)
) {
    // Appearance options
    var theme by intPreference(ConfigKey.KEY_APP_THEME, Theme.SYSTEM.ordinal)
    var materialYou by booleanPreference(ConfigKey.KEY_APP_MATERIAL_YOU, true)
    var pitchBlack by booleanPreference("pitchBlack", false)

    // General options
    var showChosenMapThumbnail by booleanPreference(ConfigKey.KEY_SHOW_CHOSEN_MAP_THUMBNAIL, true)
    var showMapThumbnailsInList by booleanPreference(ConfigKey.KEY_SHOW_MAP_THUMBNAILS_LIST, true)
    var language by stringPreference(ConfigKey.KEY_APP_LANGUAGE, "") // follow system if blank

    // Directory options
    var lacMapsDir by stringPreference(ConfigKey.KEY_MAPS_DIR, ConfigKey.RECOMMENDED_MAPS_DIR)
    var lacWallpapersDir by stringPreference(ConfigKey.KEY_WALLPAPERS_DIR, ConfigKey.RECOMMENDED_WALLPAPERS_DIR)
    var lacScreenshotsDir by stringPreference(ConfigKey.KEY_SCREENSHOTS_DIR, ConfigKey.RECOMMENDED_SCREENSHOTS_DIR)
    var exportedMapsDir by stringPreference(ConfigKey.KEY_EXPORTED_MAPS_DIR, ConfigKey.RECOMMENDED_EXPORTED_MAPS_DIR)
    var storageAccessType by intPreference("storageAccessType", StorageAccessType.SAF.ordinal)

    // Experimental (developer) options
    var experimentalOptionsEnabled by booleanPreference("experimentalOptionsEnabled", false)
    var showMapNameFieldGuide by booleanPreference("showMapNameFieldGuide", true)
    var autoCheckUpdates by booleanPreference(ConfigKey.KEY_APP_AUTO_UPDATES, true)
    var updatesURL by stringPreference(ConfigKey.KEY_APP_UPDATES_URL, ConfigKey.DEFAULT_UPDATES_URL)
}