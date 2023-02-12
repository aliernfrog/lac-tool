package com.aliernfrog.lactool

import android.os.Environment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.data.PrefEditItem
import com.aliernfrog.lactool.data.Social

val AppRoundnessSize = 28.dp
val AppComponentShape = RoundedCornerShape(AppRoundnessSize)
const val experimentalSettingsRequiredClicks = 10

object ConfigKey {
    const val PREF_NAME = "APP_CONFIG"
    const val KEY_APP_THEME = "appTheme"
    const val KEY_APP_MATERIAL_YOU = "materialYou"
    const val KEY_APP_AUTO_UPDATES = "autoUpdates"
    const val KEY_APP_LAST_ALPHA_ACK = "lastAlphaAck"
    const val KEY_APP_UPDATES_URL = "updatesUrl"
    const val KEY_SHOW_MAP_THUMBNAILS_LIST = "showMapThumbnailsList"
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
    val socials = listOf(
        Social("LAC Discord", "https://discord.gg/aQhGqHSc3W"),
        Social("LAC Tool GitHub", "https://github.com/aliernfrog/lac-tool")
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
    val experimentalPrefOptions = listOf(
        PrefEditItem(ConfigKey.KEY_APP_UPDATES_URL, ConfigKey.DEFAULT_UPDATES_URL),
        PrefEditItem(ConfigKey.KEY_MAPS_DIR, ConfigKey.DEFAULT_MAPS_DIR),
        PrefEditItem(ConfigKey.KEY_MAPS_EXPORT_DIR, ConfigKey.DEFAULT_MAPS_EXPORT_DIR),
        PrefEditItem(ConfigKey.KEY_WALLPAPERS_DIR, ConfigKey.DEFAULT_WALLPAPERS_DIR),
        PrefEditItem(ConfigKey.KEY_SCREENSHOTS_DIR, ConfigKey.DEFAULT_SCREENSHOTS_DIR)
    )
}