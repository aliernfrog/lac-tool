package com.aliernfrog.lactool

import android.os.Environment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.data.Screen
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

object NavRoutes {
    const val MAPS = "maps"
    const val MAPS_EDIT = "mapsEdit"
    const val OPTIONS = "options"
}

@Composable
fun getScreens(): List<Screen> {
    val context = LocalContext.current
    return listOf(
        Screen(NavRoutes.MAPS, context.getString(R.string.manageMaps), rememberVectorPainter(Icons.Default.PinDrop), rememberVectorPainter(Icons.Outlined.PinDrop)),
        Screen(NavRoutes.MAPS_EDIT, context.getString(R.string.mapsEdit), null, null, true),
        Screen(NavRoutes.OPTIONS, context.getString(R.string.options), rememberVectorPainter(Icons.Default.Settings), rememberVectorPainter(Icons.Outlined.Settings))
    )
}

object PickMapSheetSegments {
    const val IMPORTED = 0
    const val EXPORTED = 1
}

object Theme {
    const val SYSTEM = 0
    const val LIGHT = 1
    const val DARK = 2
}