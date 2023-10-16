package com.aliernfrog.lactool.ui.screen.screenshots

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.PermissionData
import com.aliernfrog.lactool.ui.screen.PermissionsScreen
import com.aliernfrog.lactool.ui.viewmodel.ScreenshotsViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun ScreenshotsPermissionsScreen(
    screenshotsViewModel: ScreenshotsViewModel = getViewModel()
) {
    val permissions = remember { arrayOf(
        PermissionData(
            titleId = R.string.screenshots_permissions,
            recommendedPath = ConfigKey.DEFAULT_SCREENSHOTS_DIR,
            recommendedPathDescriptionId = R.string.screenshots_permissions_recommendedPath_description,
            doesntExistHintId = R.string.permissions_recommendedFolder_takeInGameScreenshotToCreate,
            getUri = { screenshotsViewModel.prefs.lacScreenshotsDir },
            onUriUpdate = { screenshotsViewModel.prefs.lacScreenshotsDir = it.toString() },
            content = {
                Text(stringResource(R.string.screenshots_permissions_description))
            }
        )
    ) }

    PermissionsScreen(*permissions) {
        ScreenshotsScreen()
    }
}