package com.aliernfrog.lactool.ui.screen.maps

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.ui.screen.permissions.PermissionsScreen
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import com.aliernfrog.lactool.util.MapsNavigationBackStack
import com.aliernfrog.lactool.util.slideTransitionMetadata
import io.github.aliernfrog.pftool_shared.data.PermissionData
import io.github.aliernfrog.pftool_shared.ui.dialog.CustomMessageDialog
import io.github.aliernfrog.shared.ui.dialog.DeleteConfirmationDialog
import io.github.aliernfrog.shared.ui.screen.settings.SettingsDestination
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun MapsScreen(
    vm: MapsViewModel = koinViewModel(),
    onNavigateRequest: (Any) -> Unit
) {
    val permissions = remember { arrayOf(
        PermissionData(
            title = R.string.maps_permissions,
            pref = vm.prefs.lacMapsDir,
            recommendedPathDescription = R.string.maps_permissions_recommendedPath_description,
            recommendedPathWarning = R.string.permissions_recommendedFolder_openLACToCreate,
            useUnrecommendedAnywayDescription = R.string.info_useUnrecommendedAnyway_description,
            content = {
                Text(stringResource(R.string.maps_permissions_description))
            }
        ),
        PermissionData(
            title = R.string.maps_permissions_exported,
            pref = vm.prefs.exportedMapsDir,
            recommendedPathDescription = R.string.maps_permissions_exported_recommendedPath_description,
            forceRecommendedPath = false,
            content = {
                Text(stringResource(R.string.maps_permissions_exported_description))
            }
        )
    ) }

    PermissionsScreen(
        *permissions,
        title = stringResource(R.string.maps),
        onNavigateRequest = onNavigateRequest
    ) {
        MapsScreenSafePermissions(
            onNavigateSettingsRequest = {
                onNavigateRequest(SettingsDestination.root)
            }
        )
    }
}

@Composable
private fun MapsScreenSafePermissions(
    vm: MapsViewModel = koinViewModel(),
    onNavigateSettingsRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    NavDisplay(
        backStack = vm.mapsBackStack.backStack,
        entryProvider = entryProvider {
            entry(MapsNavigationBackStack.Companion.MapsList) {
                MapsListScreen(
                    title = stringResource(R.string.maps),
                    onNavigateSettingsRequest = onNavigateSettingsRequest,
                    onBackClick = null,
                    onMapPick = {
                        vm.viewMapDetails(it)
                    }
                )
            }

            entry<MapFile>(
                metadata = slideTransitionMetadata
            ) {
                MapDetailsScreen(
                    map = it,
                    onNavigateSettingsRequest = onNavigateSettingsRequest,
                    onNavigateBackRequest = {
                        vm.mapsBackStack.removeLast()
                    }
                )
            }
        }
    )

    vm.customDialogTitleAndText?.let { (title, text) ->
        CustomMessageDialog(
            title = title,
            text = text,
            icon = Icons.Default.PriorityHigh,
            onDismissRequest = {
                vm.customDialogTitleAndText = null
            }
        )
    }

    vm.mapsPendingDelete?.let { maps ->
        DeleteConfirmationDialog(
            name = maps.joinToString(", ") { it.name },
            onDismissRequest = { vm.mapsPendingDelete = null },
            onConfirmDelete = { scope.launch {
                vm.deletePendingMaps(context)
            } }
        )
    }
}