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
import com.aliernfrog.lactool.ui.screen.maps.MapsEditScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsMaterialsScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsMergeScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsRolesScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsScreen
import com.aliernfrog.lactool.ui.sheet.*
import com.aliernfrog.lactool.ui.theme.LACToolTheme
import com.aliernfrog.lactool.ui.theme.Theme
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.ui.viewmodel.MapsEditViewModel
import com.aliernfrog.lactool.ui.viewmodel.MapsMergeViewModel
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import com.aliernfrog.lactool.ui.viewmodel.ScreenshotsViewModel
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
    private lateinit var wallpapersState: WallpapersState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = getSharedPreferences(ConfigKey.PREF_NAME, MODE_PRIVATE)
        topToastState = TopToastState(window.decorView)
        wallpapersState = WallpapersState(topToastState, config)
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

            if (mainViewModel.prefs.autoCheckUpdates) mainViewModel.checkUpdates()
        }
    }

    @OptIn(ExperimentalLayoutApi::class, ExperimentalAnimationApi::class)
    @Composable
    private fun BaseScaffold(
        mainViewModel: MainViewModel = getViewModel(),
        mapsViewModel: MapsViewModel = getViewModel(),
        mapsEditViewModel: MapsEditViewModel = getViewModel(),
        mapsMergeViewModel: MapsMergeViewModel = getViewModel(),
        screenshotsViewModel: ScreenshotsViewModel = getViewModel()
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
                    PermissionsScreen(mapsViewModel.mapsDir) {
                        MapsScreen(
                            onNavigateRequest = { destination ->
                                navController.navigate(destination.route)
                            }
                        )
                    }
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
                composable(Destination.WALLPAPERS.route) { PermissionsScreen(wallpapersState.wallpapersDir) { WallpapersScreen(wallpapersState) } }
                composable(Destination.SCREENSHOTS.route) {
                    PermissionsScreen(screenshotsViewModel.screenshotsDir) {
                        ScreenshotScreen()
                    }
                }
                composable(Destination.SETTINGS.route) {
                    SettingsScreen()
                }
            }
        }
        PickMapSheet(
            onFilePick = {
                mapsViewModel.getMap(it)
                true
            }
        )
        PickMapSheet(
            sheetState = mapsMergeViewModel.pickMapSheetState,
            onFilePick = {
                scope.launch {
                    mapsMergeViewModel.addMap(it, context)
                }
                true
            }
        )
        RoleSheet(
            role = mapsEditViewModel.roleSheetChosenRole,
            state = mapsEditViewModel.roleSheetState,
            topToastState = mapsEditViewModel.topToastState,
            onDeleteRole = { mapsEditViewModel.deleteRole(it, context) }
        )
        AddRoleSheet(
            state = mapsEditViewModel.addRoleSheetState,
            onRoleAdd = { mapsEditViewModel.addRole(it, context) }
        )
        DownloadableMaterialSheet(
            material = mapsEditViewModel.materialSheetChosenMaterial,
            failed = mapsEditViewModel.materialSheetMaterialFailed,
            state = mapsEditViewModel.materialSheetState,
            topToastState = mapsEditViewModel.topToastState,
            onDeleteRequest = { mapsEditViewModel.deleteDownloadableMaterial(it, context) },
            onError = { mapsEditViewModel.materialSheetMaterialFailed = true }
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
            screenshot = screenshotsViewModel.screenshotSheetScreeenshot,
            state = screenshotsViewModel.screenshotSheetState,
            onShareRequest = { scope.launch { screenshotsViewModel.shareImportedScreenshot(it, context) } },
            onDeleteRequest = { scope.launch { screenshotsViewModel.deleteImportedScreenshot(it) } }
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