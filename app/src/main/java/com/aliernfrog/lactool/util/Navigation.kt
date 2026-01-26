package com.aliernfrog.lactool.util

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.PinDrop
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.ui.NavDisplay
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.util.extension.removeLastIfMultiple
import io.github.aliernfrog.shared.ui.screen.settings.SettingsDestination
import io.github.aliernfrog.shared.ui.screen.settings.category
import io.github.aliernfrog.shared.util.SharedString

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

object UpdateScreenDestination

enum class NavigationBarType {
    HIDDEN,
    BOTTOM_BAR,
    SIDE_RAIL
}

class MapsNavigationBackStack {
    companion object {
        object MapsList
    }

    private val _backStack = mutableStateListOf<Any>(MapsList)

    val backStack: List<Any>
        get() = _backStack

    fun add(map: MapFile) {
        _backStack.add(map)
    }

    fun removeLast() {
        _backStack.removeLastIfMultiple()
    }

    fun removeIf(predicate: (MapFile) -> Boolean) {
        _backStack.removeIf {
            it is MapFile && predicate(it)
        }
    }
}

class AppSettingsDestination {
    companion object {
        val maps = SettingsDestination(
            title = SharedString.fromResId(R.string.settings_maps),
            description = SharedString.fromResId(R.string.settings_maps_description),
            icon = Icons.Rounded.PinDrop,
            iconContainerColor = Color.Green
        )

        val storage = SettingsDestination(
            title = SharedString.fromResId(R.string.settings_storage),
            description = SharedString.fromResId(R.string.settings_storage_description),
            icon = Icons.Rounded.FolderOpen,
            iconContainerColor = Color.Blue
        )

        val language = SettingsDestination(
            title = SharedString.fromResId(R.string.settings_language),
            description = SharedString.fromResId(R.string.settings_language_description),
            icon = Icons.Rounded.Translate,
            iconContainerColor = Color.Magenta
        )
    }
}

val appSettingsCategories = listOf(
    category(
        title = SharedString.fromResId(R.string.settings_category_game)
    ) {
        +AppSettingsDestination.maps
        +AppSettingsDestination.storage
    },

    category(
        title = SharedString.fromResId(R.string.settings_category_app)
    ) {
        +SettingsDestination.appearance
        +AppSettingsDestination.language
        +SettingsDestination.experimental
        +SettingsDestination.about
    }
)

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

val slideVerticalTransitionMetadata = NavDisplay.transitionSpec {
    slideInVertically(
        initialOffsetY = { it }
    ) + fadeIn() togetherWith slideOutVertically(
        targetOffsetY = { -it }
    ) + fadeOut()
} + NavDisplay.popTransitionSpec {
    slideInVertically(
        initialOffsetY = { -it }
    ) + fadeIn() togetherWith slideOutVertically(
        targetOffsetY = { -it }
    ) + fadeOut()
} + NavDisplay.predictivePopTransitionSpec {
    slideInVertically(
        initialOffsetY = { -it }
    ) + fadeIn() togetherWith slideOutVertically(
        targetOffsetY = { it }
    ) + fadeOut()
}