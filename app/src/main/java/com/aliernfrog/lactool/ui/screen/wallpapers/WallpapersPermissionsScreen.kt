package com.aliernfrog.lactool.ui.screen.wallpapers

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.PermissionData
import com.aliernfrog.lactool.ui.screen.PermissionsScreen
import com.aliernfrog.lactool.ui.viewmodel.WallpapersViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun WallpapersPermissionsScreen(
    wallpapersViewModel: WallpapersViewModel = getViewModel()
) {
    val permissions = remember { arrayOf(
        PermissionData(
            titleId = R.string.wallpapers_permissions,
            recommendedPath = ConfigKey.DEFAULT_WALLPAPERS_DIR,
            recommendedPathDescriptionId = R.string.wallpapers_permissions_recommendedPath_description,
            // TODO confirm LAC creates wallpaper folder automatically
            doesntExistHintId = R.string.permissions_recommendedFolder_openLACToCreate,
            getUri = { wallpapersViewModel.prefs.lacWallpapersDir },
            onUriUpdate = { wallpapersViewModel.prefs.lacWallpapersDir = it.toString() },
            content = {
                Text(stringResource(R.string.wallpapers_permissions_description))
            }
        )
    ) }

    PermissionsScreen(*permissions) {
        WallpapersScreen()
    }
}