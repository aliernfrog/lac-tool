package com.aliernfrog.lactool

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.aliernfrog.lactool.state.*
import com.aliernfrog.lactool.ui.component.BaseScaffold
import com.aliernfrog.lactool.ui.component.SheetBackHandler
import com.aliernfrog.lactool.ui.dialog.AlphaWarningDialog
import com.aliernfrog.lactool.ui.dialog.UpdateDialog
import com.aliernfrog.lactool.ui.screen.*
import com.aliernfrog.lactool.ui.sheet.*
import com.aliernfrog.lactool.ui.theme.LACToolTheme
import com.aliernfrog.lactool.ui.theme.Theme
import com.aliernfrog.lactool.util.Destination
import com.aliernfrog.lactool.util.NavigationConstant
import com.aliernfrog.lactool.util.getScreens
import com.aliernfrog.toptoast.component.TopToastHost
import com.aliernfrog.toptoast.state.TopToastState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var config: SharedPreferences
    private lateinit var topToastState: TopToastState
    private lateinit var settingsState: SettingsState
    private lateinit var updateState: UpdateState
    private lateinit var mapsState: MapsState
    private lateinit var wallpapersState: WallpapersState
    private lateinit var screenshotsState: ScreenshotsState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        config = getSharedPreferences(ConfigKey.PREF_NAME, MODE_PRIVATE)
        topToastState = TopToastState()
        settingsState = SettingsState(topToastState, config)
        updateState = UpdateState(topToastState, config, applicationContext)
        mapsState = MapsState(topToastState, config)
        wallpapersState = WallpapersState(topToastState, config)
        screenshotsState = ScreenshotsState(topToastState, config)
        setContent {
            val darkTheme = getDarkThemePreference()
            LACToolTheme(darkTheme, settingsState.materialYou.value) {
                BaseScaffold()
                TopToastHost(topToastState)
                SystemBars(darkTheme)
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class, ExperimentalAnimationApi::class)
    @Composable
    private fun BaseScaffold() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val navController = rememberAnimatedNavController()
        val screens = getScreens()
        BaseScaffold(screens, navController) {
            AnimatedNavHost(
                navController = navController,
                startDestination = NavigationConstant.INITIAL_DESTINATION,
                modifier = Modifier.fillMaxSize().padding(it).consumeWindowInsets(it),
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
                composable(Destination.MAPS.route) { PermissionsScreen(mapsState.mapsDir) { MapsScreen(mapsState, navController) } }
                composable(Destination.MAPS_EDIT.route) { MapsEditScreen(mapsState.mapsEditState, navController) }
                composable(Destination.MAPS_ROLES.route) { MapsRolesScreen(mapsState.mapsEditState, navController) }
                composable(Destination.MAPS_MERGE.route) { MapsMergeScreen(mapsState.mapsMergeState, navController) }
                composable(Destination.WALLPAPERS.route) { PermissionsScreen(wallpapersState.wallpapersDir) { WallpapersScreen(wallpapersState) } }
                composable(Destination.SCREENSHOTS.route) { PermissionsScreen(screenshotsState.screenshotsDir) { ScreenshotScreen(screenshotsState) } }
                composable(Destination.SETTINGS.route) { SettingsScreen(config, updateState, settingsState) }
            }
            SheetBackHandler(
                mapsState.pickMapSheetState,
                mapsState.mapsMergeState.pickMapSheetState,
                mapsState.mapsEditState.roleSheetState,
                mapsState.mapsEditState.addRoleSheetState,
                wallpapersState.wallpaperSheetState,
                screenshotsState.screenshotSheetState
            )
        }
        PickMapSheet(
            mapsState = mapsState,
            topToastState = topToastState,
            sheetState = mapsState.pickMapSheetState,
            showMapThumbnails = settingsState.showMapThumbnailsInList.value,
            onFilePick = { mapsState.getMap(file = it) },
            onDocumentFilePick = { mapsState.getMap(documentFile = it) }
        )
        PickMapSheet(
            mapsState = mapsState,
            topToastState = topToastState,
            sheetState = mapsState.mapsMergeState.pickMapSheetState,
            showMapThumbnails = settingsState.showMapThumbnailsInList.value,
            onFilePick = { scope.launch { mapsState.mapsMergeState.addMap(it, context) } },
            onDocumentFilePick = { scope.launch { mapsState.mapsMergeState.addMap(it, context) } }
        )
        RoleSheet(
            role = mapsState.mapsEditState.roleSheetChosenRole.value,
            state = mapsState.mapsEditState.roleSheetState,
            topToastState = topToastState,
            onDeleteRole = { mapsState.mapsEditState.deleteRole(it, context) }
        )
        AddRoleSheet(
            state = mapsState.mapsEditState.addRoleSheetState,
            onRoleAdd = { mapsState.mapsEditState.addRole(it, context) }
        )
        WallpaperSheet(
            wallpaper = wallpapersState.wallpaperSheetWallpaper.value,
            wallpapersPath = wallpapersState.wallpapersDir,
            state = wallpapersState.wallpaperSheetState,
            topToastState = topToastState,
            onShareRequest = { scope.launch { wallpapersState.shareImportedWallpaper(it, context) } },
            onDeleteRequest = { scope.launch { wallpapersState.deleteImportedWallpaper(it) } }
        )
        ScreenshotsSheet(
            screenshot = screenshotsState.screenshotSheetScreeenshot.value,
            state = screenshotsState.screenshotSheetState,
            onShareRequest = { scope.launch { screenshotsState.shareImportedScreenshot(it, context) } },
            onDeleteRequest = { scope.launch { screenshotsState.deleteImportedScreenshot(it) } }
        )
        UpdateDialog(updateState)
        AlphaWarningDialog(config)
    }

    @Composable
    private fun SystemBars(darkTheme: Boolean) {
        val controller = rememberSystemUiController()
        controller.systemBarsDarkContentEnabled = !darkTheme
        controller.isNavigationBarContrastEnforced = false
    }

    @Composable
    private fun getDarkThemePreference(): Boolean {
        return when(settingsState.theme.value) {
            Theme.LIGHT.int -> false
            Theme.DARK.int -> true
            else -> isSystemInDarkTheme()
        }
    }
}