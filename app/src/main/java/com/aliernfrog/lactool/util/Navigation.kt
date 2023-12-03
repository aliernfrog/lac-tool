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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import com.aliernfrog.lactool.R

object NavigationConstant {
    val INITIAL_DESTINATION = Destination.MAPS.route
}

enum class Destination(
    val route: String,
    val labelId: Int,
    val vectorFilled: ImageVector? = null,
    val vectorOutlined: ImageVector? = null,
    val showInNavigationBar: Boolean = true,
    val showNavigationBar: Boolean = showInNavigationBar,
    val hasNotification: MutableState<Boolean> = mutableStateOf(false)
) {
    MAPS(
        route = "maps",
        labelId = R.string.maps,
        vectorFilled = Icons.Default.PinDrop,
        vectorOutlined = Icons.Outlined.PinDrop
    ),

    MAPS_EDIT(
        route = "mapsEdit",
        labelId = R.string.mapsEdit,
        showInNavigationBar = false
    ),

    MAPS_ROLES(
        route = "mapsRoles",
        labelId = R.string.mapsRoles,
        showInNavigationBar = false
    ),

    MAPS_MATERIALS(
        route = "mapsMaterials",
        labelId = R.string.mapsMaterials,
        showInNavigationBar = false
    ),

    MAPS_MERGE(
        route = "mapsMerge",
        labelId = R.string.mapsMerge,
        showInNavigationBar = false
    ),

    WALLPAPERS(
        route = "wallpapers",
        labelId = R.string.wallpapers,
        vectorFilled = Icons.Default.Photo,
        vectorOutlined = Icons.Outlined.Photo
    ),

    SCREENSHOTS(
        route = "screenshots",
        labelId = R.string.screenshots,
        vectorFilled = Icons.Default.PhotoCamera,
        vectorOutlined = Icons.Outlined.PhotoCamera
    ),

    SETTINGS(
        route = "settings",
        labelId = R.string.settings,
        vectorFilled = Icons.Default.Settings,
        vectorOutlined = Icons.Outlined.Settings
    )
}