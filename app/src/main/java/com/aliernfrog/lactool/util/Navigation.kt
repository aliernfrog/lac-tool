package com.aliernfrog.lactool.util

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.rounded.PinDrop
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.ui.NavDisplay
import com.aliernfrog.lactool.R

object NavigationConstant {
    val INITIAL_DESTINATION = MainDestinationGroup
    val INITIAL_MAIN_DESTINATION = MainDestination.MAPS
}

object MainDestinationGroup

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

val slideTransitionMetadata = NavDisplay.transitionSpec {
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Start
    ) + fadeIn() togetherWith slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Start
    ) + fadeOut()
} + NavDisplay.popTransitionSpec {
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.End
    ) togetherWith slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.End
    )
} + NavDisplay.predictivePopTransitionSpec {
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.End
    ) togetherWith slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.End
    )
}