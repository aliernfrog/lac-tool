package com.aliernfrog.lactool.ui.screen

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.aliernfrog.lactool.ui.component.BaseScaffold
import com.aliernfrog.lactool.ui.dialog.AlphaWarningDialog
import com.aliernfrog.lactool.ui.screen.maps.MapsEditScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsMaterialsScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsMergeScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsRolesScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsScreen
import com.aliernfrog.lactool.ui.sheet.AddRoleSheet
import com.aliernfrog.lactool.ui.sheet.DownloadableMaterialSheet
import com.aliernfrog.lactool.ui.sheet.PickMapSheet
import com.aliernfrog.lactool.ui.sheet.RoleSheet
import com.aliernfrog.lactool.ui.sheet.ScreenshotsSheet
import com.aliernfrog.lactool.ui.sheet.UpdateSheet
import com.aliernfrog.lactool.ui.sheet.WallpaperSheet
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.ui.viewmodel.MapsEditViewModel
import com.aliernfrog.lactool.ui.viewmodel.MapsMergeViewModel
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import com.aliernfrog.lactool.ui.viewmodel.ScreenshotsViewModel
import com.aliernfrog.lactool.ui.viewmodel.WallpapersViewModel
import com.aliernfrog.lactool.util.Destination
import com.aliernfrog.lactool.util.NavigationConstant
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    mapsViewModel: MapsViewModel = getViewModel(),
    wallpapersViewModel: WallpapersViewModel = getViewModel(),
    screenshotsViewModel: ScreenshotsViewModel = getViewModel()
) {
    val navController = rememberAnimatedNavController()
    BaseScaffold(
        navController = navController
    ) {
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
            composable(Destination.WALLPAPERS.route) {
                PermissionsScreen(wallpapersViewModel.wallpapersDir) {
                    WallpapersScreen()
                }
            }
            composable(Destination.SCREENSHOTS.route) {
                PermissionsScreen(screenshotsViewModel.screenshotsDir) {
                    ScreenshotsScreen()
                }
            }
            composable(Destination.SETTINGS.route) {
                SettingsScreen()
            }
        }
    }
    ModalBottomSheets()
    AlphaWarningDialog()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ModalBottomSheets(
    mainViewModel: MainViewModel = getViewModel(),
    mapsViewModel: MapsViewModel = getViewModel(),
    mapsEditViewModel: MapsEditViewModel = getViewModel(),
    mapsMergeViewModel: MapsMergeViewModel = getViewModel(),
    wallpapersViewModel: WallpapersViewModel = getViewModel(),
    screenshotsViewModel: ScreenshotsViewModel = getViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    PickMapSheet(
        onMapPick = {
            mapsViewModel.chooseMap(it)
            true
        }
    )
    PickMapSheet(
        sheetState = mapsMergeViewModel.pickMapSheetState,
        onMapPick = {
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
        wallpaper = wallpapersViewModel.wallpaperSheetWallpaper,
        wallpapersPath = wallpapersViewModel.wallpapersDir,
        state = wallpapersViewModel.wallpaperSheetState,
        topToastState = wallpapersViewModel.topToastState,
        onShareRequest = { scope.launch { wallpapersViewModel.shareImportedWallpaper(it, context) } },
        onDeleteRequest = { scope.launch { wallpapersViewModel.deleteImportedWallpaper(it) } }
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
}