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
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.PinDrop
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.ui.NavDisplay
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.ui.screen.SettingsScreen
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.util.extension.enable
import com.aliernfrog.lactool.util.extension.removeLastIfMultiple
import io.github.aliernfrog.pftool_shared.ui.screen.settings.StoragePage
import io.github.aliernfrog.shared.ui.settings.LibsPage
import io.github.aliernfrog.shared.ui.settings.SettingsDestination
import org.koin.androidx.compose.koinViewModel

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

// TODO handle settings destinations in a better way
val settingsRootDestination = SettingsDestination(
    title = "",
    description = "",
    icon = Icons.Rounded.Settings,
    content = { onNavigateBackRequest, onNavigateRequest ->
        SettingsScreen(
            onNavigateRequest = onNavigateRequest,
            onNavigateBackRequest = onNavigateBackRequest
        )
    }
)

val settingsLibsDestination = SettingsDestination(
    title = "",
    description = "",
    icon = Icons.Rounded.Settings,
    content = { onNavigateBackRequest, _ ->
        LibsPage(onNavigateBackRequest = onNavigateBackRequest)
    }
)

val settingsStorageDestination = SettingsDestination(
    title = "",
    description = "",
    icon = Icons.Rounded.FolderOpen,
    iconContainerColor = Color.Blue,
    content = { onNavigateBackRequest, _ ->
        val context = LocalContext.current
        val mainViewModel = koinViewModel<MainViewModel>()
        StoragePage(
            storageAccessTypePref = mainViewModel.prefs.storageAccessType,
            folderPrefs = mapOf(
                context.getString(R.string.settings_storage_folders_maps) to mainViewModel.prefs.lacMapsDir,
                context.getString(R.string.settings_storage_folders_exportedMaps) to mainViewModel.prefs.exportedMapsDir
            ),
            onEnableStorageAccessTypeRequest = { it.enable() },
            onNavigateBackRequest = onNavigateBackRequest
        )
    }
)

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