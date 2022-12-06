package com.aliernfrog.lactool.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.Screen

object NavigationConstant {
    val INITIAL_DESTINATION = Destination.MAPS.route
    const val LABEL_FALLBACK_ID = R.string.manageMaps
}

enum class Destination(
    val route: String,
    val labelId: Int,
    val vector: ImageVector? = null,
    val vectorSelected: ImageVector? = null,
    val isSubScreen: Boolean = false
) {
    MAPS("maps", R.string.manageMaps, Icons.Default.PinDrop, Icons.Outlined.PinDrop),
    MAPS_EDIT("mapsEdit", R.string.mapsEdit, isSubScreen = true),
    MAPS_ROLES("mapsRoles", R.string.mapsRoles, isSubScreen = true),
    OPTIONS("options", R.string.options, Icons.Default.Settings, Icons.Outlined.Settings)
}

@Composable
fun getScreens(
    navController: NavController
): List<Screen> {
    return Destination.values().map { destination ->
        Screen(
            route = destination.route,
            name = stringResource(destination.labelId),
            iconFilled = destination.vector?.let { rememberVectorPainter(it) },
            iconOutlined = destination.vectorSelected?.let { rememberVectorPainter(it) },
            isSubScreen = destination.isSubScreen
        ) {
            navController.navigateUp()
        }
    }
}