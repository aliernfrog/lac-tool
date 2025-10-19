package com.aliernfrog.lactool.ui.screen

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.aliernfrog.lactool.ui.component.BaseScaffold
import com.aliernfrog.lactool.ui.dialog.ProgressDialog
import com.aliernfrog.lactool.ui.screen.maps.MapsEditScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsMaterialsScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsMergeScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsPermissionsScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsRolesScreen
import com.aliernfrog.lactool.ui.screen.screenshots.ScreenshotsPermissionsScreen
import com.aliernfrog.lactool.ui.screen.settings.SettingsDestination
import com.aliernfrog.lactool.ui.screen.wallpapers.WallpapersPermissionsScreen
import com.aliernfrog.lactool.ui.sheet.UpdateSheet
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.util.MainDestination
import com.aliernfrog.lactool.util.SubDestination
import com.aliernfrog.lactool.util.extension.removeLastIfMultiple
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val onNavigateSettingsRequest: () -> Unit = {
        mainViewModel.navigationBackStack.add(SettingsDestination.ROOT)
    }
    val onNavigateBackRequest: () -> Unit = {
        mainViewModel.navigationBackStack.removeLastIfMultiple()
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

    BaseScaffold { paddingValues ->
        NavDisplay(
            backStack = mainViewModel.navigationBackStack,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding(),
            entryProvider = entryProvider {
                entry<MainDestination>(
                    metadata = NavDisplay.transitionSpec {
                        scaleIn(
                            animationSpec = tween(delayMillis = 100),
                            initialScale = 0.95f
                        ) + fadeIn(
                            animationSpec = tween(delayMillis = 100)
                        ) togetherWith fadeOut(tween(100))
                    } + NavDisplay.popTransitionSpec {
                        scaleIn(
                            animationSpec = tween(delayMillis = 100),
                            initialScale = 1.05f
                        ) + fadeIn(
                            animationSpec = tween(delayMillis = 100)
                        ) togetherWith scaleOut(
                            animationSpec = tween(100),
                            targetScale = 0.95f
                        ) + fadeOut(
                            animationSpec = tween(100)
                        )
                    } + NavDisplay.predictivePopTransitionSpec {
                        scaleIn(
                            animationSpec = tween(delayMillis = 100),
                            initialScale = 1.05f
                        ) + fadeIn(
                            animationSpec = tween(delayMillis = 100)
                        ) togetherWith scaleOut(
                            animationSpec = tween(100),
                            targetScale = 0.95f
                        ) + fadeOut(
                            animationSpec = tween(100)
                        )
                    }
                ) { destination ->
                    when (destination) {
                        MainDestination.MAPS -> {
                            MapsPermissionsScreen(
                                onNavigateSettingsRequest = onNavigateSettingsRequest
                            )
                        }
                        MainDestination.WALLPAPERS -> {
                            WallpapersPermissionsScreen(
                                onNavigateSettingsRequest = onNavigateSettingsRequest
                            )
                        }
                        MainDestination.SCREENSHOTS -> {
                            ScreenshotsPermissionsScreen(
                                onNavigateSettingsRequest = onNavigateSettingsRequest
                            )
                        }
                    }
                }

                entry<SubDestination>(
                    metadata = slideTransitionMetadata
                ) { destination ->
                    when (destination) {
                        SubDestination.MAPS_EDIT -> {
                            MapsEditScreen(
                                onNavigateBackRequest = onNavigateBackRequest
                            )
                        }
                        SubDestination.MAPS_MERGE -> {
                            MapsMergeScreen(
                                onNavigateBackRequest = onNavigateBackRequest
                            )
                        }
                        SubDestination.MAPS_ROLES -> {
                            MapsRolesScreen(
                                onNavigateBackRequest = onNavigateBackRequest
                            )
                        }
                        SubDestination.MAPS_MATERIALS -> {
                            MapsMaterialsScreen(
                                onNavigateBackRequest = onNavigateBackRequest
                            )
                        }
                    }
                }

                entry<SettingsDestination>(
                    metadata = slideTransitionMetadata
                ) { destination ->
                    destination.content(
                        /* onNavigateBackRequest = */ {
                            mainViewModel.navigationBackStack.removeLastIfMultiple()
                        },
                        /* onNavigateRequest */ {
                            mainViewModel.navigationBackStack.add(it)
                        }
                    )
                }
            }
        )
    }

    UpdateSheet(
        sheetState = mainViewModel.updateSheetState,
        latestVersionInfo = mainViewModel.latestVersionInfo,
        updateAvailable = mainViewModel.updateAvailable,
        onCheckUpdatesRequest = { scope.launch {
            mainViewModel.checkUpdates(manuallyTriggered = true)
        } }
    )

    mainViewModel.progressState.currentProgress?.let {
        ProgressDialog(it) {}
    }
}