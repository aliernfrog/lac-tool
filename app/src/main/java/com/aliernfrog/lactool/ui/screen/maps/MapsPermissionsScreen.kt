package com.aliernfrog.lactool.ui.screen.maps

import androidx.compose.animation.AnimatedContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.PermissionData
import com.aliernfrog.lactool.ui.dialog.CustomMessageDialog
import com.aliernfrog.lactool.ui.dialog.DeleteConfirmationDialog
import com.aliernfrog.lactool.ui.screen.permissions.PermissionsScreen
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun MapsPermissionsScreen(
    mapsViewModel: MapsViewModel = koinViewModel(),
    onNavigateSettingsRequest: () -> Unit
) {
    val permissions = remember { arrayOf(
        PermissionData(
            title = R.string.maps_permissions,
            recommendedPath = ConfigKey.RECOMMENDED_MAPS_DIR,
            recommendedPathDescription = R.string.maps_permissions_recommendedPath_description,
            recommendedPathWarning = R.string.permissions_recommendedFolder_openLACToCreate,
            useUnrecommendedAnywayDescription = R.string.info_useUnrecommendedAnyway_description,
            getUri = { mapsViewModel.prefs.lacMapsDir },
            onUriUpdate = { mapsViewModel.prefs.lacMapsDir = it.toString() },
            content = {
                Text(stringResource(R.string.maps_permissions_description))
            }
        ),
        PermissionData(
            title = R.string.maps_permissions_exported,
            recommendedPath = ConfigKey.RECOMMENDED_EXPORTED_MAPS_DIR,
            recommendedPathDescription = R.string.maps_permissions_exported_recommendedPath_description,
            forceRecommendedPath = false,
            getUri = { mapsViewModel.prefs.exportedMapsDir },
            onUriUpdate = { mapsViewModel.prefs.exportedMapsDir = it.toString() },
            content = {
                Text(stringResource(R.string.maps_permissions_exported_description))
            }
        )
    ) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    PermissionsScreen(
        *permissions,
        title = stringResource(R.string.maps),
        onNavigateSettingsRequest = onNavigateSettingsRequest
    ) {
        AnimatedContent(mapsViewModel.mapListShown) { showMapList ->
            if (showMapList) MapsListScreen(
                onNavigateSettingsRequest = onNavigateSettingsRequest,
                onBackClick = if (mapsViewModel.mapListBackButtonShown) {
                    { mapsViewModel.mapListShown = false }
                } else null,
                onMapPick = {
                    mapsViewModel.chooseMap(it)
                    mapsViewModel.mapListShown = false
                }
            )
            else MapsScreen(
                onNavigateSettingsRequest = onNavigateSettingsRequest
            )
        }
    }

    mapsViewModel.customDialogTitleAndText?.let { (title, text) ->
        CustomMessageDialog(
            title = title,
            text = text,
            icon = Icons.Default.PriorityHigh,
            onDismissRequest = {
                mapsViewModel.customDialogTitleAndText = null
            }
        )
    }

    mapsViewModel.mapsPendingDelete?.let { maps ->
        DeleteConfirmationDialog(
            name = maps.joinToString(", ") { it.name },
            onDismissRequest = { mapsViewModel.mapsPendingDelete = null },
            onConfirmDelete = { scope.launch {
                mapsViewModel.deletePendingMaps(context)
            } }
        )
    }
}