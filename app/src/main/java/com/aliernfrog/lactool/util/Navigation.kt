package com.aliernfrog.lactool.util

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.rounded.PinDrop
import androidx.compose.ui.graphics.vector.ImageVector
import com.aliernfrog.lactool.R

object NavigationConstant {
    val INITIAL_DESTINATION = MainDestination.MAPS
}

enum class MainDestination(
    @StringRes val label: Int,
    val vectorFilled: ImageVector,
    val vectorOutlined: ImageVector
) {
    MAPS(
        label = R.string.maps,
        vectorFilled = Icons.Rounded.PinDrop,
        vectorOutlined = Icons.Outlined.PinDrop
    ),

    WALLPAPERS(
        label = R.string.wallpapers,
        vectorFilled = Icons.Default.Photo,
        vectorOutlined = Icons.Outlined.Photo
    ),

    SCREENSHOTS(
        label = R.string.screenshots,
        vectorFilled = Icons.Default.PhotoCamera,
        vectorOutlined = Icons.Outlined.PhotoCamera
    )
}

// TODO pass data as NavEntry type instead of this
enum class SubDestination {
    MAPS_EDIT,
    MAPS_ROLES,
    MAPS_MATERIALS,
    MAPS_MERGE
}

enum class NavigationBarType {
    HIDDEN,
    BOTTOM_BAR,
    SIDE_RAIL
}