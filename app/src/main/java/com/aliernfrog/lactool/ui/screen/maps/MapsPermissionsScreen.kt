package com.aliernfrog.lactool.ui.screen.maps

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.PermissionData
import com.aliernfrog.lactool.ui.screen.PermissionsScreen
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import com.aliernfrog.lactool.util.Destination
import org.koin.androidx.compose.koinViewModel

@Composable
fun MapsPermissionsScreen(
    onNavigateRequest: (Destination) -> Unit,
    mapsViewModel: MapsViewModel = koinViewModel()
) {
    val permissions = remember { arrayOf(
        PermissionData(
            titleId = R.string.maps_permissions,
            recommendedPath = ConfigKey.DEFAULT_MAPS_DIR,
            recommendedPathDescriptionId = R.string.maps_permissions_recommendedPath_description,
            doesntExistHintId = R.string.permissions_recommendedFolder_openLACToCreate,
            getUri = { mapsViewModel.prefs.lacMapsDir },
            onUriUpdate = { mapsViewModel.prefs.lacMapsDir = it.toString() },
            content = {
                Text(stringResource(R.string.maps_permissions_description))
            }
        ),
        PermissionData(
            titleId = R.string.maps_permissions_exported,
            recommendedPath = ConfigKey.DEFAULT_EXPORTED_MAPS_DIR,
            recommendedPathDescriptionId = R.string.maps_permissions_exported_recommendedPath_description,
            getUri = { mapsViewModel.prefs.exportedMapsDir },
            onUriUpdate = { mapsViewModel.prefs.exportedMapsDir = it.toString() },
            content = {
                Text(stringResource(R.string.maps_permissions_exported_description))
            }
        )
    ) }

    PermissionsScreen(*permissions) {
        AnimatedContent(mapsViewModel.mapListShown) { showMapList ->
            if (showMapList) MapsListScreen(
                onBackClick = if (mapsViewModel.mapListBackButtonShown) {
                    { mapsViewModel.mapListShown = false }
                } else null,
                onMapPick = {
                    mapsViewModel.chooseMap(it)
                    mapsViewModel.mapListShown = false
                }
            )
            else MapsScreen(
                onNavigateRequest = onNavigateRequest
            )
        }
    }
}