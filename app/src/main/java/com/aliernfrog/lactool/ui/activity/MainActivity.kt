package com.aliernfrog.lactool.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.aliernfrog.lactool.SettingsConstant.supportLinks
import com.aliernfrog.lactool.crashReportURL
import com.aliernfrog.lactool.ui.component.MainDestinationContent
import com.aliernfrog.lactool.ui.screen.SettingsScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsEditScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsMaterialsScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsMergeScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsRolesScreen
import com.aliernfrog.lactool.ui.theme.LACToolTheme
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.ui.viewmodel.MapsEditViewModel
import com.aliernfrog.lactool.util.MainDestinationGroup
import com.aliernfrog.lactool.util.SubDestination
import com.aliernfrog.lactool.util.UpdateScreenDestination
import com.aliernfrog.lactool.util.extension.removeLastIfMultiple
import com.aliernfrog.lactool.util.slideTransitionMetadata
import com.aliernfrog.lactool.util.slideVerticalTransitionMetadata
import com.aliernfrog.toptoast.component.TopToastHost
import io.github.aliernfrog.pftool_shared.impl.SAFFileCreator
import io.github.aliernfrog.pftool_shared.ui.dialog.ProgressDialog
import io.github.aliernfrog.pftool_shared.util.LocalPFToolSharedString
import io.github.aliernfrog.pftool_shared.util.PFToolSharedString
import io.github.aliernfrog.shared.ui.component.MediaOverlay
import io.github.aliernfrog.shared.ui.component.util.AppContainer
import io.github.aliernfrog.shared.ui.component.util.InsetsObserver
import io.github.aliernfrog.shared.ui.screen.UpdatesScreen
import io.github.aliernfrog.shared.ui.screen.settings.SettingsDestination
import io.github.aliernfrog.shared.ui.sheet.CrashDetailsSheet
import io.github.aliernfrog.shared.ui.theme.Theme
import io.github.aliernfrog.shared.util.LocalSharedString
import io.github.aliernfrog.shared.util.SharedString
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import kotlin.getValue

class MainActivity : AppCompatActivity() {
    private lateinit var safTxtFileCreator: SAFFileCreator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        safTxtFileCreator = SAFFileCreator(this, mimeType = "text/plain")
        installSplashScreen()

        val vm = getViewModel<MainViewModel>()
        val sharedString by inject<SharedString>()
        val pfToolSharedString by inject<PFToolSharedString>()

        setContent {
            val context = LocalContext.current
            val view = LocalView.current
            val useDarkTheme = shouldUseDarkTheme(vm.prefs.theme.value)
            var isAppInitialized by rememberSaveable { mutableStateOf(false) }

            @Composable
            fun AppTheme(content: @Composable () -> Unit) {
                LACToolTheme(
                    darkTheme = useDarkTheme,
                    useLightSystemBars = !useDarkTheme && vm.mediaOverlayData == null,
                    dynamicColors = vm.prefs.materialYou.value,
                    pitchBlack = vm.prefs.pitchBlack.value,
                    content = content
                )
            }

            AppTheme {
                CompositionLocalProvider(
                    LocalSharedString provides sharedString,
                    LocalPFToolSharedString provides pfToolSharedString
                ) {
                    App(vm)
                }
            }

            LaunchedEffect(Unit) {
                vm.setSafTxtFileCreator(safTxtFileCreator)
                vm.topToastState.setComposeView(view)
                if (isAppInitialized) return@LaunchedEffect

                vm.topToastState.setAppTheme { AppTheme(it) }
                this@MainActivity.intent?.let {
                    vm.handleIntent(it, context = context)
                }
                isAppInitialized = true
            }
        }
    }

    @Composable
    private fun App(vm: MainViewModel) {
        val context = LocalContext.current
        val applyImePadding = !vm.isAtMainDestination

        val availableUpdates = vm.availableUpdates.collectAsStateWithLifecycle().value
        val currentVersionInfo = vm.currentVersionInfo.collectAsStateWithLifecycle().value
        val isCompatibleWithLatestVersion = vm.isCompatibleWithLatestVersion.collectAsStateWithLifecycle().value
        val isCheckingForUpdates = vm.isCheckingForUpdates.collectAsStateWithLifecycle().value

        val onNavigateRequest: (Any) -> Unit = {
            vm.navigationBackStack.add(it)
        }

        val onNavigateBackRequest: () -> Unit = {
            vm.navigationBackStack.removeLastIfMultiple()
        }

        /**
         * This is because the current version of navigation3 stores the ViewModel instance
         * in memory unless a different key is used.
         * This may be a bug fixed in later updates, we are currently on a outdated version
         * which is the last version with minSdk 21 so:
         * TODO: take a look at this when minSdk and navigation3 is bumped
         */
        fun getUniqueNavKey() = System.nanoTime().toString()

        InsetsObserver()
        AppContainer {
            NavDisplay(
                backStack = vm.navigationBackStack,
                modifier = Modifier
                    .fillMaxSize()
                    .let {
                        // MainDestinationGroup handles imePadding, so skip it here if we are at MainDestinationGroup
                        if (applyImePadding) it.imePadding() else it
                    },
                entryProvider = entryProvider {
                    entry<MainDestinationGroup> {
                        MainDestinationContent(vm)
                    }

                    entry<SubDestination.MapsEdit.Root>(
                        metadata = slideTransitionMetadata
                    ) { key ->
                        val vmKey = rememberSaveable { getUniqueNavKey() }
                        MapsEditScreen(
                            vm = koinViewModel(
                                key = vmKey
                            ) {
                                parametersOf(key.map, onNavigateBackRequest)
                            },
                            vmKey = vmKey,
                            onNavigateRequest = onNavigateRequest
                        )
                    }

                    entry<SubDestination.MapsEdit.Roles>(
                        metadata = slideTransitionMetadata
                    ) { key ->
                        val mapsEditViewModel = koinViewModel<MapsEditViewModel>(
                            key = key.vmKey
                        )
                        MapsRolesScreen(
                            topToastState = vm.topToastState,
                            roles = mapsEditViewModel.mapEditor?.mapRoles ?: emptyList(),
                            onAddRoleRequest = {
                                mapsEditViewModel.addRole(it, context)
                            },
                            onDeleteRoleRequest = {
                                mapsEditViewModel.deleteRole(it, context)
                            },
                            onNavigateBackRequest = onNavigateBackRequest
                        )
                    }

                    entry<SubDestination.MapsEdit.Materials>(
                        metadata = slideTransitionMetadata
                    ) { key ->
                        val mapsEditViewModel = koinViewModel<MapsEditViewModel>(
                            key = key.vmKey
                        )
                        MapsMaterialsScreen(
                            listOptions = vm.prefs.mapsMaterialsListOptions,
                            materialsLoadProgress = mapsEditViewModel.materialsLoadProgress,
                            loadedMaterials = mapsEditViewModel.loadedMaterials,
                            onLoadMaterialsRequest = {
                                mapsEditViewModel.loadDownloadableMaterials(context)
                            },
                            onOpenMaterialOptionsRequest = {
                                mapsEditViewModel.openDownloadableMaterialOptions(it)
                            },
                            onNavigateBackRequest = onNavigateBackRequest
                        )
                    }

                    entry<SubDestination.MapsMerge>(
                        metadata = slideTransitionMetadata
                    ) { key ->
                        MapsMergeScreen(
                            vm = koinViewModel(
                                key = rememberSaveable { getUniqueNavKey() }
                            ) {
                                parametersOf(key.maps, onNavigateBackRequest)
                            },
                            onNavigateBackRequest = onNavigateBackRequest
                        )
                    }

                    entry<SettingsDestination>(
                        metadata = slideTransitionMetadata
                    ) { destination ->
                        SettingsScreen(
                            destination = destination,
                            onNavigateBackRequest = onNavigateBackRequest,
                            onNavigateRequest = { vm.navigationBackStack.add(it) },
                            onCheckUpdatesRequest = { skipVersionCheck ->
                                vm.checkUpdates(skipVersionCheck = skipVersionCheck)
                            },
                            onNavigateUpdatesScreenRequest = {
                                vm.navigationBackStack.add(UpdateScreenDestination)
                            }
                        )
                    }

                    entry<UpdateScreenDestination>(
                        metadata = slideVerticalTransitionMetadata
                    ) {
                        UpdatesScreen(
                            availableUpdates = availableUpdates,
                            currentVersionInfo = currentVersionInfo,
                            isCheckingForUpdates = isCheckingForUpdates,
                            isCompatibleWithLatestVersion = isCompatibleWithLatestVersion,
                            onCheckUpdatesRequest = {
                                vm.checkUpdates(manuallyTriggered = true)
                            },
                            onNavigateBackRequest = onNavigateBackRequest
                        )
                    }
                }
            )

            CrashDetailsSheet(
                throwable = vm.lastCaughtException,
                crashReportURL = crashReportURL,
                debugInfo = vm.versionManager.getDebugInfo(),
                supportLinks = supportLinks
            )

            vm.progressState.currentProgress?.let {
                ProgressDialog(it) {
                    vm.progressState.currentProgress = null
                }
            }

            Crossfade(vm.mediaOverlayData) { data ->
                if (data != null) MediaOverlay(
                    data = data,
                    showMediaOverlayGuidePref = vm.prefs.showMediaOverlayGuide,
                    onDismissRequest = { vm.dismissMediaOverlay() }
                )
            }
            TopToastHost(vm.topToastState)
        }
    }

    @Composable
    private fun shouldUseDarkTheme(theme: Int): Boolean {
        return when(theme) {
            Theme.LIGHT.ordinal -> false
            Theme.DARK.ordinal -> true
            else -> isSystemInDarkTheme()
        }
    }
}