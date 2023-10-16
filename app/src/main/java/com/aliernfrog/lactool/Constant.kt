package com.aliernfrog.lactool

import android.os.Build
import android.os.Environment
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
    const val KEY_WALLPAPERS_DIR = "wallpapersDir"
    const val KEY_SCREENSHOTS_DIR = "screenshotsDir"
    const val KEY_EXPORTED_MAPS_DIR = "mapsExportDir"
    const val DEFAULT_UPDATES_URL = "https://aliernfrog.github.io/lactool/latest.json"
    val DEFAULT_MAPS_DIR = "${externalStorageRoot}Android/data/com.MA.LAC/files/editor"
    val DEFAULT_WALLPAPERS_DIR = "${externalStorageRoot}Android/data/com.MA.LAC/files/wallpaper"
    val DEFAULT_SCREENSHOTS_DIR = "${externalStorageRoot}Android/data/com.MA.LAC/files/screenshots"
    val DEFAULT_EXPORTED_MAPS_DIR = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/LACTool/exported"
}

object SettingsConstant {
    val socials = listOf(
        Social("LAC Discord", "https://discord.gg/aQhGqHSc3W"),
        Social("LAC Tool GitHub", githubRepoURL)
    )
    val folders = listOf(
        PrefEditItem(
            labelResourceId = R.string.settings_general_folders_maps,
            getValue = { it.lacMapsDir },
            setValue = { newValue, prefs ->
                prefs.lacMapsDir = newValue
            },
            default = ConfigKey.DEFAULT_MAPS_DIR
        ),
        PrefEditItem(
            labelResourceId = R.string.settings_general_folders_wallpapers,
            getValue = { it.lacWallpapersDir },
            setValue = { newValue, prefs ->
                prefs.lacWallpapersDir = newValue
            },
            default = ConfigKey.DEFAULT_WALLPAPERS_DIR
        ),
        PrefEditItem(
            labelResourceId = R.string.settings_general_folders_screenshots,
            getValue = { it.lacScreenshotsDir },
            setValue = { newValue, prefs ->
                prefs.lacScreenshotsDir = newValue
            },
            default = ConfigKey.DEFAULT_SCREENSHOTS_DIR
        ),
        PrefEditItem(
            labelResourceId = R.string.settings_general_folders_exportedMaps,
            getValue = { it.exportedMapsDir },
            setValue = { newValue, prefs ->
              prefs.exportedMapsDir = newValue
            },
            default = ConfigKey.DEFAULT_EXPORTED_MAPS_DIR,
        )
    )
    /*val pathOptionPresets = listOf(
        PathOptionPreset(
            labelResourceId = R.string.settings_general_pathOptions_presets_default,
            lacMapsPath = ConfigKey.DEFAULT_MAPS_DIR,
            lacWallpapersPath = ConfigKey.DEFAULT_WALLPAPERS_DIR,
            lacScreenshotsPath = ConfigKey.DEFAULT_SCREENSHOTS_DIR,
            appMapsExportPath = ConfigKey.DEFAULT_MAPS_EXPORT_DIR
        ),
        PathOptionPreset(
            labelResourceId = R.string.settings_general_pathOptions_presets_lacd,
            lacMapsPath = "${externalStorageRoot}Android/data/com.MA.LACD/files/editor",
            lacWallpapersPath = "${externalStorageRoot}Android/data/com.MA.LACD/files/wallpaper",
            lacScreenshotsPath = "${externalStorageRoot}Android/data/com.MA.LACD/files/screenshots"
        ),
        PathOptionPreset(
            labelResourceId = R.string.settings_general_pathOptions_presets_lacm,
            lacMapsPath = "${externalStorageRoot}Android/data/com.MA.LACM/files/editor",
            lacWallpapersPath = "${externalStorageRoot}Android/data/com.MA.LACM/files/wallpaper",
            lacScreenshotsPath = "${externalStorageRoot}Android/data/com.MA.LACM/files/screenshots"
        ),
        PathOptionPreset(
            labelResourceId = R.string.settings_general_pathOptions_presets_lacmb,
            lacMapsPath = "${externalStorageRoot}Android/data/com.MA.LACMB/files/editor",
            lacWallpapersPath = "${externalStorageRoot}Android/data/com.MA.LACMB/files/wallpaper",
            lacScreenshotsPath = "${externalStorageRoot}Android/data/com.MA.LACMB/files/screenshots"
        )
    )*/
    val experimentalPrefOptions = listOf(
        PrefEditItem(
            labelResourceId = R.string.settings_experimental_lastAlphaAck,
            getValue = { it.lastAlphaAck },
            setValue = { newValue, prefs ->
                prefs.lastAlphaAck = newValue
            }
        ),
        PrefEditItem(
            labelResourceId = R.string.settings_experimental_updatesURL,
            getValue = { it.updatesURL },
            setValue = { newValue, prefs ->
                prefs.updatesURL = newValue
            },
            default = ConfigKey.DEFAULT_UPDATES_URL
        ),
        *folders.toTypedArray()
    )
}