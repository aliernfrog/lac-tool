package com.aliernfrog.lactool

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aliernfrog.lactool.state.MapsState
import com.aliernfrog.lactool.state.ScreenshotsState
import com.aliernfrog.lactool.state.SettingsState
import com.aliernfrog.lactool.state.WallpapersState
import com.aliernfrog.lactool.ui.component.BaseScaffold
import com.aliernfrog.lactool.ui.component.SheetBackHandler
import com.aliernfrog.lactool.ui.screen.*
import com.aliernfrog.lactool.ui.sheet.*
import com.aliernfrog.lactool.ui.theme.LACToolTheme
import com.aliernfrog.lactool.ui.theme.Theme
import com.aliernfrog.lactool.util.Destination
import com.aliernfrog.lactool.util.NavigationConstant
import com.aliernfrog.lactool.util.getScreens
import com.aliernfrog.toptoast.component.TopToastHost
import com.aliernfrog.toptoast.state.TopToastState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var config: SharedPreferences
    private lateinit var topToastState: TopToastState
    private lateinit var settingsState: SettingsState
    private lateinit var mapsState: MapsState
    private lateinit var wallpapersState: WallpapersState
    private lateinit var screenshotsState: ScreenshotsState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        config = getSharedPreferences(ConfigKey.PREF_NAME, MODE_PRIVATE)
        topToastState = TopToastState()
        settingsState = SettingsState(config)
        mapsState = MapsState(topToastState, config)
        wallpapersState = WallpapersState(topToastState, config)
        screenshotsState = ScreenshotsState(topToastState, config)
        setContent {
            val darkTheme = getDarkThemePreference()
            LACToolTheme(darkTheme, settingsState.materialYou.value) {
                TopToastHost(
                    state = topToastState,
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                ) { BaseScaffold() }
                SystemBars(darkTheme)
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun BaseScaffold() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val navController = rememberNavController()
        val screens = getScreens(navController, mapsState.mapsEditState)
        BaseScaffold(screens, navController) {
            NavHost(
                navController = navController,
                startDestination = NavigationConstant.INITIAL_DESTINATION,
                modifier = Modifier.fillMaxSize().padding(it).consumeWindowInsets(it).systemBarsPadding()
            ) {
                composable(Destination.MAPS.route) { PermissionsScreen(mapsState.mapsDir) { MapsScreen(mapsState, navController) } }
                composable(Destination.MAPS_EDIT.route) { MapsEditScreen(mapsState.mapsEditState, navController) }
                composable(Destination.MAPS_ROLES.route) { MapsRolesScreen(mapsState.mapsEditState) }
                composable(Destination.WALLPAPERS.route) { PermissionsScreen(wallpapersState.wallpapersDir) { WallpapersScreen(wallpapersState) } }
                composable(Destination.SCREENSHOTS.route) { PermissionsScreen(screenshotsState.screenshotsDir) { ScreenshotScreen(screenshotsState) } }
                composable(Destination.SETTINGS.route) { SettingsScreen(config, topToastState, settingsState) }
            }
            SheetBackHandler(
                mapsState.pickMapSheetState,
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