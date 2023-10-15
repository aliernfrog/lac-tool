package com.aliernfrog.lactool

import android.os.Build
import android.os.Environment
import com.aliernfrog.lactool.data.PathOptionPreset
import com.aliernfrog.lactool.data.PrefEditItem
import com.aliernfrog.lactool.data.Social

const val experimentalSettingsRequiredClicks = 10
const val githubRepoURL = "https://github.com/aliernfrog/lac-tool"

val externalStorageRoot = Environment.getExternalStorageDirectory().toString()+"/"
val folderPickerSupportsInitialUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

object ConfigKey {
    const val PREF_NAME = "APP_CONFIG"
    const val KEY_APP_THEME = "appTheme"
    const val KEY_APP_MATERIAL_YOU = "materialYou"
    const val KEY_APP_AUTO_UPDATES = "autoUpdates"
    const val KEY_APP_LAST_ALPHA_ACK = "lastAlphaAck"
    const val KEY_APP_UPDATES_URL = "updatesUrl"
    const val KEY_SHOW_MAP_THUMBNAILS_LIST = "showMapThumbnailsList"
    const val KEY_SHOW_CHOSEN_MAP_THUMBNAIL = "chosenMapThumbnail"
    const val KEY_MAPS_DIR = "mapsDir"
    const val KEY_MAPS_EXPORT_DIR = "mapsExportDir"
    const val KEY_WALLPAPERS_DIR = "wallpapersDir"
    const val KEY_SCREENSHOTS_DIR = "screenshotsDir"
    const val DEFAULT_UPDATES_URL = "https://aliernfrog.github.io/lactool/latest.json"
    val DEFAULT_MAPS_DIR = "${Environment.getExternalStorageDirectory()}/Android/data/com.MA.LAC/files/editor"
    val DEFAULT_MAPS_EXPORT_DIR = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/LACTool/exported"
    val DEFAULT_WALLPAPERS_DIR = "${Environment.getExternalStorageDirectory()}/Android/data/com.MA.LAC/files/wallpaper"
    val DEFAULT_SCREENSHOTS_DIR = "${Environment.getExternalStorageDirectory()}/Android/data/com.MA.LAC/files/screenshots"
}

object SettingsConstant {
    private val externalStorageRoot = Environment.getExternalStorageDirectory().absolutePath
    val socials = listOf(
        Social("LAC Discord", "https://discord.gg/aQhGqHSc3W"),
        Social("LAC Tool GitHub", githubRepoURL)
    )
    val pathOptions = listOf(
        PrefEditItem(
            key = ConfigKey.KEY_MAPS_DIR,
            default = ConfigKey.DEFAULT_MAPS_DIR,
            labelResourceId = R.string.settings_general_pathOptions_maps
        ),
        PrefEditItem(
            key = ConfigKey.KEY_WALLPAPERS_DIR,
            default = ConfigKey.DEFAULT_WALLPAPERS_DIR,
            labelResourceId = R.string.settings_general_pathOptions_wallpapers
        ),
        PrefEditItem(
            key = ConfigKey.KEY_SCREENSHOTS_DIR,
            default = ConfigKey.DEFAULT_SCREENSHOTS_DIR,
            labelResourceId = R.string.settings_general_pathOptions_screenshots
        ),
        PrefEditItem(
            key = ConfigKey.KEY_MAPS_EXPORT_DIR,
            default = ConfigKey.DEFAULT_MAPS_EXPORT_DIR,
            labelResourceId = R.string.settings_general_pathOptions_mapsExport
        )
    )
    val pathOptionPresets = listOf(
        PathOptionPreset(
            labelResourceId = R.string.settings_general_pathOptions_presets_default,
            lacMapsPath = ConfigKey.DEFAULT_MAPS_DIR,
            lacWallpapersPath = ConfigKey.DEFAULT_WALLPAPERS_DIR,
            lacScreenshotsPath = ConfigKey.DEFAULT_SCREENSHOTS_DIR,
            appMapsExportPath = ConfigKey.DEFAULT_MAPS_EXPORT_DIR
        ),
        PathOptionPreset(
            labelResourceId = R.string.settings_general_pathOptions_presets_lacd,
            lacMapsPath = "$externalStorageRoot/Android/data/com.MA.LACD/files/editor",
            lacWallpapersPath = "$externalStorageRoot/Android/data/com.MA.LACD/files/wallpaper",
            lacScreenshotsPath = "$externalStorageRoot/Android/data/com.MA.LACD/files/screenshots"
        ),
        PathOptionPreset(
            labelResourceId = R.string.settings_general_pathOptions_presets_lacm,
            lacMapsPath = "$externalStorageRoot/Android/data/com.MA.LACM/files/editor",
            lacWallpapersPath = "$externalStorageRoot/Android/data/com.MA.LACM/files/wallpaper",
            lacScreenshotsPath = "$externalStorageRoot/Android/data/com.MA.LACM/files/screenshots"
        ),
        PathOptionPreset(
            labelResourceId = R.string.settings_general_pathOptions_presets_lacmb,
            lacMapsPath = "$externalStorageRoot/Android/data/com.MA.LACMB/files/editor",
            lacWallpapersPath = "$externalStorageRoot/Android/data/com.MA.LACMB/files/wallpaper",
            lacScreenshotsPath = "$externalStorageRoot/Android/data/com.MA.LACMB/files/screenshots"
        )
    )
    val experimentalPrefOptions = listOf(
        PrefEditItem(ConfigKey.KEY_APP_LAST_ALPHA_ACK, ""),
        PrefEditItem(ConfigKey.KEY_APP_UPDATES_URL, ConfigKey.DEFAULT_UPDATES_URL)
    )
}