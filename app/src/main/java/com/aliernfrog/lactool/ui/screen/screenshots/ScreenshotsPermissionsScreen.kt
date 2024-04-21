package com.aliernfrog.lactool.ui.screen.screenshots

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.PermissionData
import com.aliernfrog.lactool.ui.screen.permissions.PermissionsScreen
import com.aliernfrog.lactool.ui.viewmodel.ScreenshotsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ScreenshotsPermissionsScreen(
    screenshotsViewModel: ScreenshotsViewModel = koinViewModel(),
    onNavigateSettingsRequest: () -> Unit
) {
    val permissions = remember { arrayOf(
        PermissionData(
            title = R.string.screenshots_permissions,
            recommendedPath = ConfigKey.RECOMMENDED_SCREENSHOTS_DIR,
            recommendedPathDescription = R.string.screenshots_permissions_recommendedPath_description,
            recommendedPathWarning = R.string.permissions_recommendedFolder_takeInGameScreenshotToCreate,
            useUnrecommendedAnywayDescription = R.string.info_useUnrecommendedAnyway_description,
            getUri = { screenshotsViewModel.prefs.lacScreenshotsDir },
            onUriUpdate = { screenshotsViewModel.prefs.lacScreenshotsDir = it.toString() },
            content = {
                Text(stringResource(R.string.screenshots_permissions_description))
            }
        )
    ) }

    PermissionsScreen(
        *permissions,
        title = stringResource(R.string.screenshots),
        onNavigateSettingsRequest = onNavigateSettingsRequest
    ) {
        ScreenshotsScreen(
            onNavigateSettingsRequest = onNavigateSettingsRequest
        )
    }
}