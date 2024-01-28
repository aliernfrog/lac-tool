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
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aliernfrog.lactool.ui.component.BaseScaffold
import com.aliernfrog.lactool.ui.dialog.AlphaWarningDialog
import com.aliernfrog.lactool.ui.dialog.ProgressDialog
import com.aliernfrog.lactool.ui.screen.maps.MapsEditScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsMaterialsScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsMergeScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsPermissionsScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsRolesScreen
import com.aliernfrog.lactool.ui.screen.screenshots.ScreenshotsPermissionsScreen
import com.aliernfrog.lactool.ui.screen.wallpapers.WallpapersPermissionsScreen
import com.aliernfrog.lactool.ui.sheet.UpdateSheet
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.util.Destination
import com.aliernfrog.lactool.util.NavigationConstant
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = koinViewModel()
) {
    val navController = rememberNavController()
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
                MapsPermissionsScreen()
            }
            composable(Destination.MAPS_EDIT.route) {
                MapsEditScreen(
                    onNavigateBackRequest = { navController.popBackStack() },
                    onNavigateRequest = { destination ->
                        navController.navigate(destination.route)
                    }
                )
            }
            composable(Destination.MAPS_ROLES.route) {
                MapsRolesScreen(
                    onNavigateBackRequest = { navController.popBackStack() }
                )
            }
            composable(Destination.MAPS_MATERIALS.route) {
                MapsMaterialsScreen(
                    onNavigateBackRequest = { navController.popBackStack() }
                )
            }
            composable(Destination.MAPS_MERGE.route) {
                MapsMergeScreen(
                    onNavigateBackRequest = { navController.popBackStack() }
                )
            }
            composable(Destination.WALLPAPERS.route) {
                WallpapersPermissionsScreen()
            }
            composable(Destination.SCREENSHOTS.route) {
                ScreenshotsPermissionsScreen()
            }
            composable(Destination.SETTINGS.route) {
                SettingsScreen()
            }
        }
    }

    UpdateSheet(
        sheetState = mainViewModel.updateSheetState,
        latestVersionInfo = mainViewModel.latestVersionInfo
    )

    LaunchedEffect(navController) {
        mainViewModel.navController = navController
    }

    mainViewModel.progressState.currentProgress?.let {
        ProgressDialog(it) {}
    }

    if (mainViewModel.showAlphaWarningDialog) AlphaWarningDialog(
        onDismissRequest = { acknowledged ->
            if (acknowledged) mainViewModel.prefs.lastAlphaAck = mainViewModel.applicationVersionName
            mainViewModel.showAlphaWarningDialog = false
        }
    )
}