package com.aliernfrog.lactool.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.aliernfrog.lactool.ui.component.MainDestinationContent
import com.aliernfrog.lactool.ui.screen.maps.MapsEditScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsMaterialsScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsMergeScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsRolesScreen
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.util.MainDestinationGroup
import com.aliernfrog.lactool.util.SubDestination
import com.aliernfrog.lactool.util.extension.removeLastIfMultiple
import com.aliernfrog.lactool.util.slideTransitionMetadata
import io.github.aliernfrog.pftool_shared.ui.dialog.ProgressDialog
import io.github.aliernfrog.shared.ui.settings.SettingsDestination
import io.github.aliernfrog.shared.ui.sheet.UpdateSheet
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    vm: MainViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val applyImePadding = !vm.isAtMainDestination

    val onNavigateBackRequest: () -> Unit = {
        vm.navigationBackStack.removeLastIfMultiple()
    }

    NavDisplay(
        backStack = vm.navigationBackStack,
        modifier = Modifier
            .fillMaxSize()
            .let {
                // MainDestinationGroup handles imePadding, so skip it here if we are at MainDestinationGroup
                if (applyImePadding) it.imePadding() else it
            },
        entryProvider = entryProvider {
            entry<MainDestinationGroup> { _ ->
                MainDestinationContent()
            }

            entry<SubDestination>(
                metadata = slideTransitionMetadata
            ) { destination ->
                when (destination) {
                    SubDestination.MAPS_EDIT -> {
                        MapsEditScreen(
                            onNavigateBackRequest = onNavigateBackRequest
                        )
                    }
                    SubDestination.MAPS_MERGE -> {
                        MapsMergeScreen(
                            onNavigateBackRequest = onNavigateBackRequest
                        )
                    }
                    SubDestination.MAPS_ROLES -> {
                        MapsRolesScreen(
                            onNavigateBackRequest = onNavigateBackRequest
                        )
                    }
                    SubDestination.MAPS_MATERIALS -> {
                        MapsMaterialsScreen(
                            onNavigateBackRequest = onNavigateBackRequest
                        )
                    }
                }
            }

            entry<SettingsDestination>(
                metadata = slideTransitionMetadata
            ) { destination ->
                SettingsScreen(
                    destination = destination,
                    onNavigateBackRequest = onNavigateBackRequest,
                    onNavigateRequest = { vm.navigationBackStack.add(it) },
                    onShowUpdateSheetRequest = { scope.launch {
                        vm.updateSheetState.show()
                    } }
                )
            }
        }
    )

    UpdateSheet(
        sheetState = vm.updateSheetState,
        latestVersionInfo = vm.latestVersionInfo.collectAsState().value,
        updateAvailable = vm.updateAvailable.collectAsState().value,
        onCheckUpdatesRequest = { scope.launch {
            vm.checkUpdates(manuallyTriggered = true)
        } }
    )

    vm.progressState.currentProgress?.let {
        ProgressDialog(it) {
            vm.progressState.currentProgress = null
        }
    }
}