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
import com.aliernfrog.lactool.ui.screen.MapsEditScreen
import com.aliernfrog.lactool.ui.screen.MapsScreenRoot
import com.aliernfrog.lactool.ui.screen.OptionsScreen
import com.aliernfrog.lactool.ui.sheet.DeleteMapSheet
import com.aliernfrog.lactool.ui.sheet.PickMapSheet
import com.aliernfrog.lactool.ui.theme.LACToolTheme
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
        LACToolBaseScaffold(navController) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.MAPS,
                modifier = Modifier.fillMaxSize().padding(it).consumedWindowInsets(it).systemBarsPadding()
            ) {
                composable(route = NavRoutes.MAPS) { MapsScreenRoot(mapsState, navController) }
                composable(route = NavRoutes.MAPS_EDIT) { MapsEditScreen(mapsState.mapsEditState, navController) }
                composable(route = NavRoutes.OPTIONS) { OptionsScreen(config, topToastManager, optionsState) }
            }
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
            Theme.LIGHT -> false
            Theme.DARK -> true
            else -> isSystemInDarkTheme()
        }
    }
}