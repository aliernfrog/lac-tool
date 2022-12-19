package com.aliernfrog.lactool.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.Screen
import com.aliernfrog.lactool.state.MapsEditState
import kotlinx.coroutines.launch

object NavigationConstant {
    val INITIAL_DESTINATION = Destination.MAPS.route
    const val LABEL_FALLBACK_ID = R.string.maps
}

enum class Destination(
    val route: String,
    val labelId: Int,
    val vector: ImageVector? = null,
    val vectorSelected: ImageVector? = null,
    val isSubScreen: Boolean = false
) {
    MAPS("maps", R.string.maps, Icons.Default.PinDrop, Icons.Outlined.PinDrop),
    MAPS_EDIT("mapsEdit", R.string.mapsEdit, isSubScreen = true),
    MAPS_ROLES("mapsRoles", R.string.mapsRoles, isSubScreen = true),
    WALLPAPERS("wallpapers", R.string.wallpapers, Icons.Default.Photo, Icons.Outlined.Photo),
    SCREENSHOTS("screenshots", R.string.screenshots, Icons.Default.PhotoCamera, Icons.Outlined.PhotoCamera),
    SETTINGS("settings", R.string.settings, Icons.Default.Settings, Icons.Outlined.Settings)
}

@Composable
fun getScreens(
    navController: NavController,
    mapsEditState: MapsEditState
): List<Screen> {
    val scope = rememberCoroutineScope()
    return Destination.values().map { destination ->
        Screen(
            route = destination.route,
            name = stringResource(destination.labelId),
            iconFilled = destination.vector?.let { rememberVectorPainter(it) },
            iconOutlined = destination.vectorSelected?.let { rememberVectorPainter(it) },
            isSubScreen = destination.isSubScreen
        ) {
            when(destination) {
                Destination.MAPS_EDIT -> {
                    scope.launch { mapsEditState.onNavigationBack(navController) }
                }
                else -> navController.navigateUp()
            }
        }
    }
}