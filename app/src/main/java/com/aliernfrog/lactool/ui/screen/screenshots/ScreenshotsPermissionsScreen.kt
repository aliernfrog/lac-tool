package com.aliernfrog.lactool.ui.screen.screenshots

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.screen.permissions.PermissionsScreen
import com.aliernfrog.lactool.ui.viewmodel.ScreenshotsViewModel
import io.github.aliernfrog.pftool_shared.data.PermissionData
import io.github.aliernfrog.shared.ui.settings.SettingsDestination
import org.koin.androidx.compose.koinViewModel

@Composable
fun ScreenshotsPermissionsScreen(
    vm: ScreenshotsViewModel = koinViewModel(),
    onNavigateRequest: (Any) -> Unit
) {
    val permissions = remember { arrayOf(
        PermissionData(
            title = R.string.screenshots_permissions,
            pref = vm.prefs.lacScreenshotsDir,
            recommendedPathDescription = R.string.screenshots_permissions_recommendedPath_description,
            recommendedPathWarning = R.string.permissions_recommendedFolder_takeInGameScreenshotToCreate,
            useUnrecommendedAnywayDescription = R.string.info_useUnrecommendedAnyway_description,
            content = {
                Text(stringResource(R.string.screenshots_permissions_description))
            }
        )
    ) }

    PermissionsScreen(
        *permissions,
        title = stringResource(R.string.screenshots),
        onNavigateRequest = onNavigateRequest
    ) {
        ScreenshotsScreen(
            onNavigateSettingsRequest = {
                onNavigateRequest(SettingsDestination.root)
            }
        )
    }
}