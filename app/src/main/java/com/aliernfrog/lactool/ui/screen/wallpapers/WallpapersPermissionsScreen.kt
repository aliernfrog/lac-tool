package com.aliernfrog.lactool.ui.screen.wallpapers

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.screen.permissions.PermissionsScreen
import com.aliernfrog.lactool.ui.viewmodel.WallpapersViewModel
import io.github.aliernfrog.pftool_shared.data.PermissionData
import io.github.aliernfrog.shared.ui.screen.settings.SettingsDestination
import org.koin.androidx.compose.koinViewModel

@Composable
fun WallpapersPermissionsScreen(
    vm: WallpapersViewModel = koinViewModel(),
    onNavigateRequest: (Any) -> Unit
) {
    val permissions = remember { arrayOf(
        PermissionData(
            title = R.string.wallpapers_permissions,
            pref = vm.prefs.lacWallpapersDir,
            recommendedPathDescription = R.string.wallpapers_permissions_recommendedPath_description,
            recommendedPathWarning = R.string.permissions_recommendedFolder_importWallpaperToCreate,
            useUnrecommendedAnywayDescription = R.string.info_useUnrecommendedAnyway_description,
            content = {
                Text(stringResource(R.string.wallpapers_permissions_description))
            }
        )
    ) }

    PermissionsScreen(
        *permissions,
        title = stringResource(R.string.wallpapers),
        onNavigateRequest = onNavigateRequest
    ) {
        WallpapersScreen(
            vm = vm,
            onNavigateSettingsRequest = {
                onNavigateRequest(SettingsDestination.root)
            }
        )
    }
}