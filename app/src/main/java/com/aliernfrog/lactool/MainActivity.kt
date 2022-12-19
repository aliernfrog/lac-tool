package com.aliernfrog.lactool

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
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
import com.aliernfrog.lactool.state.SettingsState
import com.aliernfrog.lactool.state.WallpapersState
import com.aliernfrog.lactool.ui.component.BaseScaffold
import com.aliernfrog.lactool.ui.component.SheetBackHandler
import com.aliernfrog.lactool.ui.screen.*
import com.aliernfrog.lactool.ui.sheet.AddRoleSheet
import com.aliernfrog.lactool.ui.sheet.PickMapSheet
import com.aliernfrog.lactool.ui.sheet.RoleSheet
import com.aliernfrog.lactool.ui.sheet.WallpaperSheet
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
    private lateinit var pickMapSheetState: ModalBottomSheetState
    private lateinit var mapsState: MapsState
    private lateinit var wallpapersState: WallpapersState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        config = getSharedPreferences(ConfigKey.PREF_NAME, MODE_PRIVATE)
        topToastState = TopToastState()
        settingsState = SettingsState(config)
        pickMapSheetState = ModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        mapsState = MapsState(topToastState, config, pickMapSheetState)
        wallpapersState = WallpapersState(topToastState, config)
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
                composable(route = Destination.MAPS.route) { PermissionsScreen(mapsState.mapsDir) { MapsScreen(mapsState = mapsState, navController = navController) } }
                composable(route = Destination.MAPS_EDIT.route) { MapsEditScreen(mapsState.mapsEditState, navController) }
                composable(route = Destination.MAPS_ROLES.route) { MapsRolesScreen(mapsState.mapsEditState) }
                composable(route = Destination.WALLPAPERS.route) { PermissionsScreen(wallpapersState.wallpapersDir) { WallpapersScreen(wallpapersState) } }
                composable(route = Destination.SETTINGS.route) { SettingsScreen(config, topToastState, settingsState) }
            }
            SheetBackHandler(
                pickMapSheetState,
                mapsState.mapsEditState.roleSheetState,
                mapsState.mapsEditState.addRoleSheetState,
                wallpapersState.wallpaperSheetState
            )
        }
        PickMapSheet(
            mapsState = mapsState,
            topToastState = topToastState,
            sheetState = pickMapSheetState,
            showMapThumbnails = settingsState.showMapThumbnailsInList.value,
            onFilePick = { mapsState.getMap(file = it, context = context) },
            onDocumentFilePick = { mapsState.getMap(documentFile = it, context = context) }
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
            onDeleteRequest = { scope.launch { wallpapersState.deleteImportedWallpaper(it, context) } }
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