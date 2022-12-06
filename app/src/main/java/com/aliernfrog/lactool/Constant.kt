package com.aliernfrog.lactool

import android.os.Environment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.data.Social

val LACToolRoundnessSize = 30.dp
val LACToolComposableShape = RoundedCornerShape(LACToolRoundnessSize)

object ConfigKey {
    const val PREF_NAME = "APP_CONFIG"
    const val KEY_APP_THEME = "appTheme"
    const val KEY_APP_MATERIAL_YOU = "materialYou"
    const val KEY_SHOW_MAP_THUMBNAILS_LIST = "showMapThumbnailsList"
    const val KEY_MAPS_DIR = "mapsDir"
    const val KEY_MAPS_EXPORT_DIR = "mapsExportDir"
    val DEFAULT_MAPS_DIR = "${Environment.getExternalStorageDirectory()}/Android/data/com.MA.LAC/files/editor"
    val DEFAULT_MAPS_EXPORT_DIR = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/LACTool/exported"
}

object Link {
    val socials = listOf(
        Social("LAC Discord", "https://discord.gg/aQhGqHSc3W"),
        Social("LAC Tool GitHub", "https://github.com/aliernfrog/lac-tool")
    )
}