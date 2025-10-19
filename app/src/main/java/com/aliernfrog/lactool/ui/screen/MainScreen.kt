package com.aliernfrog.lactool.ui.screen

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.aliernfrog.lactool.ui.component.MainDestinationContent
import com.aliernfrog.lactool.ui.dialog.ProgressDialog
import com.aliernfrog.lactool.ui.screen.maps.MapsEditScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsMaterialsScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsMergeScreen
import com.aliernfrog.lactool.ui.screen.maps.MapsRolesScreen
import com.aliernfrog.lactool.ui.screen.settings.SettingsDestination
import com.aliernfrog.lactool.ui.sheet.UpdateSheet
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.util.MainDestinationGroup
import com.aliernfrog.lactool.util.SubDestination
import com.aliernfrog.lactool.util.extension.removeLastIfMultiple
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val applyImePadding = !mainViewModel.isAtMainDestination

    val onNavigateBackRequest: () -> Unit = {
        mainViewModel.navigationBackStack.removeLastIfMultiple()
    }

    val slideTransitionMetadata = NavDisplay.transitionSpec {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Start
        ) + fadeIn() togetherWith slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Start
        ) + fadeOut()
    } + NavDisplay.popTransitionSpec {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.End
        ) togetherWith slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.End
        )
    } + NavDisplay.predictivePopTransitionSpec {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.End
        ) togetherWith slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.End
        )
    }

    NavDisplay(
        backStack = mainViewModel.navigationBackStack,
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
                destination.content(
                    /* onNavigateBackRequest = */ {
                        onNavigateBackRequest()
                    },
                    /* onNavigateRequest */ {
                        mainViewModel.navigationBackStack.add(it)
                    }
                )
            }
        }
    )

    UpdateSheet(
        sheetState = mainViewModel.updateSheetState,
        latestVersionInfo = mainViewModel.latestVersionInfo,
        updateAvailable = mainViewModel.updateAvailable,
        onCheckUpdatesRequest = { scope.launch {
            mainViewModel.checkUpdates(manuallyTriggered = true)
        } }
    )

    mainViewModel.progressState.currentProgress?.let {
        ProgressDialog(it) {}
    }
}