package com.aliernfrog.lactool.util

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.PinDrop
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.NavDisplay
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.domain.MapsState
import com.aliernfrog.lactool.domain.WallpapersState
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.util.extension.removeLastIfMultiple
import io.github.aliernfrog.pftool_shared.data.FileExtension
import io.github.aliernfrog.pftool_shared.ui.component.maps.MapsListFABMenu
import io.github.aliernfrog.pftool_shared.ui.component.maps.MapsListFABToggleButton
import io.github.aliernfrog.shared.ui.screen.settings.SettingsDestination
import io.github.aliernfrog.shared.ui.screen.settings.category
import io.github.aliernfrog.shared.util.SharedStringResolvable
import org.koin.compose.koinInject

object NavigationConstant {
    val INITIAL_DESTINATION = MainDestinationGroup
    val INITIAL_MAIN_DESTINATION = MainDestination.MAPS
}

object MainDestinationGroup

enum class MainDestination(
    @StringRes val label: Int,
    val vectorFilled: ImageVector,
    val vectorOutlined: ImageVector,
    val floatingActionButton: (@Composable (modifier: Modifier, isFloatingBar: Boolean) -> Unit)? = null,
    val fabMenuForFloatingBar: (@Composable (modifier: Modifier) -> Unit)? = null
) {
    MAPS(
        label = R.string.maps,
        vectorFilled = Icons.Rounded.PinDrop,
        vectorOutlined = Icons.Outlined.PinDrop,
        floatingActionButton = { modifier, isFloatingBar ->
            val mapsState = koinInject<MapsState>()
            if (isFloatingBar) MapsListFABToggleButton(
                expanded = mapsState.addMapMenuExpanded,
                onExpandedStateChange = { mapsState.addMapMenuExpanded = it },
                modifier = modifier.offset(x = 4.dp)
            ) else MapsListFABMenu(
                expanded = mapsState.addMapMenuExpanded,
                supportedFileExtensions = listOf(
                    FileExtension(
                        extension = ".txt",
                        mimeType = "text/plain"
                    )
                ),
                modifier = modifier,
                onExpandedStateChange = { mapsState.addMapMenuExpanded = it },
                onMapPick = { mapsState.mapsBackStack.add(MapFile(it)) }
            )
        },
        fabMenuForFloatingBar = { modifier ->
            val mapsState = koinInject<MapsState>()
            MapsListFABMenu(
                expanded = mapsState.addMapMenuExpanded,
                supportedFileExtensions = listOf(
                    FileExtension(
                        extension = ".txt",
                        mimeType = "text/plain"
                    )
                ),
                modifier = modifier.offset(y = (-32).dp),
                onExpandedStateChange = { mapsState.addMapMenuExpanded = it },
                onMapPick = { mapsState.mapsBackStack.add(MapFile(it)) },
                button = {
                    // Not needed, floating bar has its own FAB
                }
            )
        }
    ),

    WALLPAPERS(
        label = R.string.wallpapers,
        vectorFilled = Icons.Default.Wallpaper,
        vectorOutlined = Icons.Outlined.Wallpaper,
        floatingActionButton = { modifier, _ ->
            val wallpapersState = koinInject<WallpapersState>()
            val context = LocalContext.current
            val mediaPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia(),
                onResult = { uri ->
                    if (uri != null) wallpapersState.onWallpaperPick(uri, context)
                }
            )

            FloatingActionButton(
                onClick = {
                    mediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = modifier
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.wallpapers_add)
                )
            }
        }
    ),

    SCREENSHOTS(
        label = R.string.screenshots,
        vectorFilled = Icons.Default.Camera,
        vectorOutlined = Icons.Outlined.Camera
    )
}

sealed class SubDestination {
    sealed class MapsEdit : SubDestination() {
        data class Root(val map: MapFile) : MapsEdit()

        data class Roles(val vmKey: String) : MapsEdit()

        data class Materials(val vmKey: String) : MapsEdit()
    }

    data class MapsMerge(val maps: List<MapFile>) : SubDestination()
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

    val backStack: List<Any>
        field = mutableStateListOf<Any>(MapsList)

    fun add(map: MapFile) {
        backStack.add(map)
    }

    fun removeLast() {
        backStack.removeLastIfMultiple()
    }

    fun removeIf(predicate: (MapFile) -> Boolean) {
        backStack.removeIf {
            it is MapFile && predicate(it)
        }
    }
}

class AppSettingsDestination {
    companion object {
        val maps = SettingsDestination(
            title = SharedStringResolvable.Resource(R.string.settings_maps),
            description = SharedStringResolvable.Resource(R.string.settings_maps_description),
            icon = Icons.Rounded.PinDrop,
            iconContainerColor = Color.Green
        )

        val storage = SettingsDestination(
            title = SharedStringResolvable.Resource(R.string.settings_storage),
            description = SharedStringResolvable.Resource(R.string.settings_storage_description),
            icon = Icons.Rounded.FolderOpen,
            iconContainerColor = Color.Blue
        )

        val language = SettingsDestination(
            title = SharedStringResolvable.Resource(R.string.settings_language),
            description = SharedStringResolvable.Resource(R.string.settings_language_description),
            icon = Icons.Rounded.Translate,
            iconContainerColor = Color.Magenta
        )
    }
}

val appSettingsCategories = listOf(
    category(
        title = SharedStringResolvable.Resource(R.string.settings_category_game)
    ) {
        +AppSettingsDestination.maps
        +AppSettingsDestination.storage
    },

    category(
        title = SharedStringResolvable.Resource(R.string.settings_category_app)
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