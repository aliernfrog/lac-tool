package com.aliernfrog.lactool.ui.activity

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.state.*
import com.aliernfrog.lactool.ui.component.BaseScaffold
import com.aliernfrog.lactool.ui.dialog.AlphaWarningDialog
import com.aliernfrog.lactool.ui.screen.*
import com.aliernfrog.lactool.ui.sheet.*
import com.aliernfrog.lactool.ui.theme.LACToolTheme
import com.aliernfrog.lactool.ui.theme.Theme
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import com.aliernfrog.lactool.util.Destination
import com.aliernfrog.lactool.util.NavigationConstant
import com.aliernfrog.lactool.util.getScreens
import com.aliernfrog.toptoast.component.TopToastHost
import com.aliernfrog.toptoast.state.TopToastState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterialApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var config: SharedPreferences
    private lateinit var topToastState: TopToastState
    private lateinit var mapsState: MapsState
    private lateinit var wallpapersState: WallpapersState
    private lateinit var screenshotsState: ScreenshotsState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = getSharedPreferences(ConfigKey.PREF_NAME, MODE_PRIVATE)
        topToastState = TopToastState(window.decorView)
        mapsState = MapsState(topToastState, config)
        wallpapersState = WallpapersState(topToastState, config)
        screenshotsState = ScreenshotsState(topToastState, config)
        setContent {
            AppContent()
        }
    }

    @Composable
    private fun AppContent(
        mainViewModel: MainViewModel = getViewModel()
    ) {
        val view = LocalView.current
        val scope = rememberCoroutineScope()
        val darkTheme = getDarkThemePreference(mainViewModel.prefs.theme)
        LACToolTheme(
            darkTheme = darkTheme,
            dynamicColors = mainViewModel.prefs.materialYou
        ) {
            BaseScaffold()
            TopToastHost(mainViewModel.topToastState)
        }
        LaunchedEffect(Unit) {
            mainViewModel.scope = scope
            mainViewModel.topToastState.setComposeView(view)

            if (mainViewModel.prefs.autoCheckUpdates) mainViewModel.checkForUpdates()
        }
    }

    @OptIn(ExperimentalLayoutApi::class, ExperimentalAnimationApi::class)
    @Composable
    private fun BaseScaffold(
        mainViewModel: MainViewModel = getViewModel(),
        mapsViewModel: MapsViewModel = getViewModel()
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val navController = rememberAnimatedNavController()
        val screens = getScreens()
        BaseScaffold(screens, navController) {
            AnimatedNavHost(
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
                    PermissionsScreen(mapsState.mapsDir) {
                        MapsScreen(
                            onNavigateRequest = { destination ->
                                navController.navigate(destination.route)
                            }
                        )
                    }
                }
                composable(Destination.MAPS_EDIT.route) { MapsEditScreen(mapsState.mapsEditState, navController) }
                composable(Destination.MAPS_ROLES.route) { MapsRolesScreen(mapsState.mapsEditState, navController) }
                composable(Destination.MAPS_MATERIALS.route) { MapsMaterialsScreen(mapsState.mapsEditState, navController) }
                composable(Destination.MAPS_MERGE.route) { MapsMergeScreen(mapsState.mapsMergeState, navController) }
                composable(Destination.WALLPAPERS.route) { PermissionsScreen(wallpapersState.wallpapersDir) { WallpapersScreen(wallpapersState) } }
                composable(Destination.SCREENSHOTS.route) { PermissionsScreen(screenshotsState.screenshotsDir) { ScreenshotScreen(screenshotsState) } }
                composable(Destination.SETTINGS.route) {
                    SettingsScreen()
                }
            }
        }
        PickMapSheet(
            onFilePick = {
                mapsViewModel.getMap(it)
            }
        )
        PickMapSheet(
            sheetState = mapsState.mapsMergeState.pickMapSheetState,
            onFilePick = { scope.launch {
                mapsState.mapsMergeState.addMap(it, context)
            } }
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
        DownloadableMaterialSheet(
            material = mapsState.mapsEditState.materialSheetChosenMaterial.value,
            failed = mapsState.mapsEditState.materialSheetMaterialFailed.value,
            state = mapsState.mapsEditState.materialSheetState,
            topToastState = topToastState,
            onDeleteRequest = { mapsState.mapsEditState.deleteDownloadableMaterial(it, context) },
            onError = { mapsState.mapsEditState.materialSheetMaterialFailed.value = true }
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
        UpdateSheet(
            sheetState = mainViewModel.updateSheetState,
            latestVersionInfo = mainViewModel.latestVersionInfo
        )
        AlphaWarningDialog(config)
    }

    @Composable
    private fun getDarkThemePreference(theme: Int): Boolean {
        return when(theme) {
            Theme.LIGHT.int -> false
            Theme.DARK.int -> true
            else -> isSystemInDarkTheme()
        }
    }
}