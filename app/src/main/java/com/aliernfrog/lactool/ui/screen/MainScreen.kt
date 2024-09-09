package com.aliernfrog.lactool.ui.screen

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aliernfrog.lactool.ui.component.BaseScaffold
import com.aliernfrog.lactool.ui.dialog.ProgressDialog
import com.aliernfrog.lactool.ui.screen.maps.MapsEditScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsMaterialsScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsMergeScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsPermissionsScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsRolesScreen
import com.aliernfrog.lactool.ui.screen.screenshots.ScreenshotsPermissionsScreen
import com.aliernfrog.lactool.ui.screen.settings.SettingsScreen
import com.aliernfrog.lactool.ui.screen.wallpapers.WallpapersPermissionsScreen
import com.aliernfrog.lactool.ui.sheet.UpdateSheet
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.util.Destination
import com.aliernfrog.lactool.util.NavigationConstant
import com.aliernfrog.lactool.util.extension.popBackStackSafe
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val onNavigateSettingsRequest: () -> Unit = {
        navController.navigate(Destination.SETTINGS.route)
    }
    val onNavigateBackRequest: () -> Unit = {
        navController.popBackStackSafe()
    }

    BaseScaffold(
        navController = navController
    ) {
        NavHost(
            navController = navController,
            startDestination = NavigationConstant.INITIAL_DESTINATION,
            modifier = Modifier.fillMaxSize().padding(it).consumeWindowInsets(it).imePadding(),
            enterTransition = { scaleIn(
                animationSpec = tween(delayMillis = 100),
                initialScale = 0.95f
            ) + fadeIn(
                animationSpec = tween(delayMillis = 100)
            ) },
            exitTransition = { fadeOut(tween(100)) },
            popEnterTransition = { scaleIn(
                animationSpec = tween(delayMillis = 100),
                initialScale = 1.05f
            ) + fadeIn(
                animationSpec = tween(delayMillis = 100)
            ) },
            popExitTransition = { scaleOut(
                animationSpec = tween(100),
                targetScale = 0.95f
            ) + fadeOut(
                animationSpec = tween(100)
            ) }
        ) {
            composable(Destination.MAPS.route) {
                MapsPermissionsScreen(
                    onNavigateSettingsRequest = onNavigateSettingsRequest
                )
            }
            composable(Destination.MAPS_EDIT.route) {
                MapsEditScreen(
                    onNavigateBackRequest = onNavigateBackRequest,
                    onNavigateRequest = { destination ->
                        navController.navigate(destination.route)
                    }
                )
            }
            composable(Destination.MAPS_ROLES.route) {
                MapsRolesScreen(
                    onNavigateBackRequest = onNavigateBackRequest
                )
            }
            composable(Destination.MAPS_MATERIALS.route) {
                MapsMaterialsScreen(
                    onNavigateBackRequest = onNavigateBackRequest
                )
            }
            composable(Destination.MAPS_MERGE.route) {
                MapsMergeScreen(
                    onNavigateBackRequest = onNavigateBackRequest
                )
            }
            composable(Destination.WALLPAPERS.route) {
                WallpapersPermissionsScreen(
                    onNavigateSettingsRequest = onNavigateSettingsRequest
                )
            }
            composable(Destination.SCREENSHOTS.route) {
                ScreenshotsPermissionsScreen(
                    onNavigateSettingsRequest = onNavigateSettingsRequest
                )
            }
            composable(Destination.SETTINGS.route) {
                SettingsScreen(
                    onNavigateBackRequest = onNavigateBackRequest
                )
            }
        }
    }

    UpdateSheet(
        sheetState = mainViewModel.updateSheetState,
        latestVersionInfo = mainViewModel.latestVersionInfo,
        updateAvailable = mainViewModel.updateAvailable,
        onCheckUpdatesRequest = { scope.launch {
            mainViewModel.checkUpdates(manuallyTriggered = true)
        } },
        onIgnoreRequest = {
            mainViewModel.prefs.ignoredUpdateVersionCode.value = mainViewModel.latestVersionInfo.versionCode
        }
    )

    LaunchedEffect(navController) {
        mainViewModel.navController = navController
    }

    mainViewModel.progressState.currentProgress?.let {
        ProgressDialog(it) {}
    }
}