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
import com.aliernfrog.lactool.state.OptionsState
import com.aliernfrog.lactool.ui.composable.LACToolBaseScaffold
import com.aliernfrog.lactool.ui.composable.LACToolSheetBackHandler
import com.aliernfrog.lactool.ui.screen.*
import com.aliernfrog.lactool.ui.sheet.AddRoleSheet
import com.aliernfrog.lactool.ui.sheet.DeleteMapSheet
import com.aliernfrog.lactool.ui.sheet.PickMapSheet
import com.aliernfrog.lactool.ui.sheet.RoleSheet
import com.aliernfrog.lactool.ui.theme.LACToolTheme
import com.aliernfrog.lactool.ui.theme.Theme
import com.aliernfrog.lactool.util.Destination
import com.aliernfrog.lactool.util.NavigationConstant
import com.aliernfrog.lactool.util.getScreens
import com.aliernfrog.toptoast.TopToastBase
import com.aliernfrog.toptoast.TopToastManager
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var config: SharedPreferences
    private lateinit var topToastManager: TopToastManager
    private lateinit var optionsState: OptionsState
    private lateinit var pickMapSheetState: ModalBottomSheetState
    private lateinit var deleteMapSheetState: ModalBottomSheetState
    private lateinit var mapsState: MapsState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        config = getSharedPreferences(ConfigKey.PREF_NAME, MODE_PRIVATE)
        topToastManager = TopToastManager()
        optionsState = OptionsState(config)
        pickMapSheetState = ModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        deleteMapSheetState = ModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, isSkipHalfExpanded = true)
        mapsState = MapsState(topToastManager, config, pickMapSheetState, deleteMapSheetState)
        setContent {
            val darkTheme = getDarkThemePreference()
            LACToolTheme(darkTheme, optionsState.materialYou.value) {
                TopToastBase(backgroundColor = MaterialTheme.colorScheme.background, manager = topToastManager) { BaseScaffold() }
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
        val screens = getScreens(navController)
        LACToolBaseScaffold(screens, navController) {
            NavHost(
                navController = navController,
                startDestination = NavigationConstant.INITIAL_DESTINATION,
                modifier = Modifier.fillMaxSize().padding(it).consumedWindowInsets(it).systemBarsPadding()
            ) {
                composable(route = Destination.MAPS.route) { PermissionsScreen(mapsState.mapsDir) { MapsScreen(mapsState = mapsState, navController = navController) } }
                composable(route = Destination.MAPS_EDIT.route) { MapsEditScreen(mapsState.mapsEditState, navController) }
                composable(route = Destination.MAPS_ROLES.route) { MapsRolesScreen(mapsState.mapsEditState) }
                composable(route = Destination.OPTIONS.route) { OptionsScreen(config, topToastManager, optionsState) }
            }
            LACToolSheetBackHandler(
                pickMapSheetState,
                deleteMapSheetState,
                mapsState.mapsEditState.roleSheetState,
                mapsState.mapsEditState.addRoleSheetState
            )
        }
        PickMapSheet(
            mapsState = mapsState,
            topToastManager = topToastManager,
            sheetState = pickMapSheetState,
            showMapThumbnails = optionsState.showMapThumbnailsInList.value,
            onFilePick = { mapsState.getMap(file = it, context = context) },
            onDocumentFilePick = { mapsState.getMap(documentFile = it, context = context) }
        )
        DeleteMapSheet(
            mapName = mapsState.lastMapName.value,
            sheetState = deleteMapSheetState
        ) {
            scope.launch { mapsState.deleteChosenMap(context) }
        }
        RoleSheet(
            role = mapsState.mapsEditState.roleSheetChosenRole.value,
            state = mapsState.mapsEditState.roleSheetState,
            topToastManager = topToastManager,
            onDeleteRole = { mapsState.mapsEditState.deleteRole(it, context) }
        )
        AddRoleSheet(
            state = mapsState.mapsEditState.addRoleSheetState,
            onRoleAdd = { mapsState.mapsEditState.addRole(it, context) }
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
        return when(optionsState.theme.value) {
            Theme.LIGHT.int -> false
            Theme.DARK.int -> true
            else -> isSystemInDarkTheme()
        }
    }
}