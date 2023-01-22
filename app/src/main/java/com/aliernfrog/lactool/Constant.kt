package com.aliernfrog.lactool

import android.os.Environment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.data.LACMapObjectFilter
import com.aliernfrog.lactool.data.Social

val AppRoundnessSize = 30.dp
val AppComponentShape = RoundedCornerShape(AppRoundnessSize)

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

object Link {
    val socials = listOf(
        Social("LAC Discord", "https://discord.gg/aQhGqHSc3W"),
        Social("LAC Tool GitHub", "https://github.com/aliernfrog/lac-tool")
    )
}

object LACMapObjectFilters {
    val defaultFilters = listOf(
        LACMapObjectFilter(
            query = mutableStateOf("Trigger_Box_Editor"),
            caseSensitive = mutableStateOf(true),
            exactMatch = mutableStateOf(true),
            labelStringId = R.string.mapsEdit_filterObjects_defaultFilters_triggerBox
        ),
        LACMapObjectFilter(
            query = mutableStateOf("Team_"),
            caseSensitive = mutableStateOf(true),
            exactMatch = mutableStateOf(false),
            labelStringId = R.string.mapsEdit_filterObjects_defaultFilters_tdm
        ),
        LACMapObjectFilter(
            query = mutableStateOf("Checkpoint_Editor"),
            caseSensitive = mutableStateOf(true),
            exactMatch = mutableStateOf(false),
            labelStringId = R.string.mapsEdit_filterObjects_defaultFilters_racing
        )
    )
}