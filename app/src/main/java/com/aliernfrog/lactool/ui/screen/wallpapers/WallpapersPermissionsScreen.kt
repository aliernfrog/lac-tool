package com.aliernfrog.lactool.ui.screen.wallpapers

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.PermissionData
import com.aliernfrog.lactool.ui.screen.permissions.PermissionsScreen
import com.aliernfrog.lactool.ui.viewmodel.WallpapersViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun WallpapersPermissionsScreen(
    wallpapersViewModel: WallpapersViewModel = koinViewModel(),
    onNavigateSettingsRequest: () -> Unit
) {
    val permissions = remember { arrayOf(
        PermissionData(
            title = R.string.wallpapers_permissions,
            recommendedPath = ConfigKey.RECOMMENDED_WALLPAPERS_DIR,
            recommendedPathDescription = R.string.wallpapers_permissions_recommendedPath_description,
            recommendedPathWarning = R.string.permissions_recommendedFolder_importWallpaperToCreate,
            useUnrecommendedAnywayDescription = R.string.info_useUnrecommendedAnyway_description,
            getUri = { wallpapersViewModel.prefs.lacWallpapersDir.value },
            onUriUpdate = { wallpapersViewModel.prefs.lacWallpapersDir.value = it.toString() },
            content = {
                Text(stringResource(R.string.wallpapers_permissions_description))
            }
        )
    ) }

    PermissionsScreen(
        *permissions,
        title = stringResource(R.string.wallpapers),
        onNavigateSettingsRequest = onNavigateSettingsRequest
    ) {
        WallpapersScreen(
            onNavigateSettingsRequest = onNavigateSettingsRequest
        )
    }
}